/*
 * Copyright 2018 Jeremy KUHN
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.winterframework.core.compiler.socket;

import java.util.Collections;
import java.util.List;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.ModuleElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;

import io.winterframework.core.annotation.Bean;
import io.winterframework.core.annotation.Configuration;
import io.winterframework.core.annotation.Selector;
import io.winterframework.core.annotation.WiredTo;
import io.winterframework.core.compiler.ModuleAnnotationProcessor;
import io.winterframework.core.compiler.spi.BeanQualifiedName;
import io.winterframework.core.compiler.spi.MultiSocketType;
import io.winterframework.core.compiler.spi.SocketBeanInfo;

/**
 * <p>
 * A {@link SocketBeanInfoFactory} implementation used by the
 * {@link ModuleAnnotationProcessor} to create {@link SocketBeanInfo} for
 * component modules required in other modules (possibly compiled modules).
 * </p>
 * 
 * @author jkuhn
 *
 */
class BinarySocketBeanInfoFactory extends SocketBeanInfoFactory {

	private ModuleElement compiledModuleElement;
	
	private TypeMirror supplierType;
	private TypeMirror optionalType;
	private TypeMirror configurationSocketType;
	
	private TypeMirror beanAnnotationType;
	private TypeMirror configurationAnnotationType;
	
	private TypeMirror wiredToAnnotationType;
	
	/**
	 * @param processingEnvironment
	 * @param moduleElement
	 */
	public BinarySocketBeanInfoFactory(ProcessingEnvironment processingEnvironment, ModuleElement moduleElement, ModuleElement compiledModuleElement) {
		super(processingEnvironment, moduleElement);
		
		this.compiledModuleElement = compiledModuleElement;
		this.supplierType = this.processingEnvironment.getTypeUtils().erasure(this.processingEnvironment.getElementUtils().getTypeElement(Supplier.class.getCanonicalName()).asType());
		this.optionalType = this.processingEnvironment.getTypeUtils().erasure(this.processingEnvironment.getElementUtils().getTypeElement(Optional.class.getCanonicalName()).asType());
		this.configurationSocketType = this.processingEnvironment.getTypeUtils().erasure(this.processingEnvironment.getElementUtils().getTypeElement("io.winterframework.core.v1.Module.ConfigurationSocket").asType());
		this.beanAnnotationType = this.processingEnvironment.getElementUtils().getTypeElement(Bean.class.getCanonicalName()).asType();
		this.configurationAnnotationType = this.processingEnvironment.getElementUtils().getTypeElement(Configuration.class.getCanonicalName()).asType();
		this.wiredToAnnotationType = this.processingEnvironment.getElementUtils().getTypeElement(WiredTo.class.getCanonicalName()).asType();
	}

	@Override
	public WirableSocketBeanInfo createSocketBean(Element element) throws SocketCompilationException {
		if(!element.getKind().equals(ElementKind.PARAMETER)) {
			throw new IllegalArgumentException("Element must be a parameter");
		}
		
		VariableElement variableElement = (VariableElement)element;
		ExecutableElement socketElement = (ExecutableElement)variableElement.getEnclosingElement();
		if(!((TypeElement)socketElement.getEnclosingElement()).getQualifiedName().toString().equals(this.moduleQName.getClassName())) {
			throw new IllegalArgumentException("The specified element doesn't belong to module " + this.moduleQName);
		}
		
		TypeMirror socketType = variableElement.asType();
		final AbstractSocketBeanInfo moduleSocketInfo;
		if(this.processingEnvironment.getTypeUtils().isSameType(this.processingEnvironment.getTypeUtils().erasure(socketType), this.configurationSocketType)) {
			moduleSocketInfo = this.createConfigurationSocketBean((DeclaredType)socketType, socketElement);
		}
		else {
			moduleSocketInfo = this.createSocketBean(element, socketType, socketElement);
		}
		
		moduleSocketInfo.setWiredBeans(this.extractWiredBeans(variableElement));
		
		return moduleSocketInfo;
	}

	@SuppressWarnings("unchecked")
	private Set<BeanQualifiedName> extractWiredBeans(VariableElement element) {
		return element.getAnnotationMirrors().stream()
			.filter(a -> this.processingEnvironment.getTypeUtils().isSameType(a.getAnnotationType(), this.wiredToAnnotationType))
			.flatMap(a -> {
				List<String> wiredBeanQNames = Collections.emptyList();			
				for(Entry<? extends ExecutableElement, ? extends AnnotationValue> value : this.processingEnvironment.getElementUtils().getElementValuesWithDefaults(a).entrySet()) {
					switch(value.getKey().getSimpleName().toString()) {
						case "value" : wiredBeanQNames = ((List<AnnotationValue>)value.getValue().getValue()).stream().map(v -> (String)v.getValue()).collect(Collectors.toList());
							break;
					}
				}
				return wiredBeanQNames.stream();
			})
			.map(name -> new BeanQualifiedName(this.moduleQName, name))
			.collect(Collectors.toSet());
	}
	
	private AbstractSocketBeanInfo createSocketBean(Element element, TypeMirror socketType, ExecutableElement socketElement) throws SocketCompilationException {
		boolean optional = false;
		if(this.processingEnvironment.getTypeUtils().isSameType(this.processingEnvironment.getTypeUtils().erasure(socketType), this.optionalType)) {
			optional = true;
			socketType = ((DeclaredType)socketType).getTypeArguments().get(0);
		}
		
		TypeElement socketTypeElement = (TypeElement)this.processingEnvironment.getTypeUtils().asElement(socketType);
		
		if(socketTypeElement.getKind() != ElementKind.INTERFACE) {
			throw new IllegalArgumentException("A socket bean element must be an interface");
		}
		
		Optional<? extends AnnotationMirror> annotation = socketTypeElement.getAnnotationMirrors().stream().filter(a -> this.processingEnvironment.getTypeUtils().isSameType(a.getAnnotationType(), this.beanAnnotationType)).findFirst();
		if(!annotation.isPresent()) {
			throw new IllegalArgumentException("The specified element is not annotated with " + Bean.class.getSimpleName());
		}
		
		String socketName = null;
		Bean.Visibility visibility = null;
		for(Entry<? extends ExecutableElement, ? extends AnnotationValue> value : this.processingEnvironment.getElementUtils().getElementValuesWithDefaults(annotation.get()).entrySet()) {
			switch(value.getKey().getSimpleName().toString()) {
				case "name" : socketName = (String)value.getValue().getValue();
					break;
				case "visibility" : visibility = Bean.Visibility.valueOf(value.getValue().getValue().toString());
					break;
			}
		}
		
		if(visibility.equals(Bean.Visibility.PRIVATE)) {
			throw new IllegalArgumentException("A socket bean must be public");
		}
		
		Optional<? extends TypeMirror> supplierType = socketTypeElement.getInterfaces().stream().filter(t -> this.processingEnvironment.getTypeUtils().isSameType(this.processingEnvironment.getTypeUtils().erasure(t), this.supplierType)).findFirst();
		if(!supplierType.isPresent()) {
			throw new IllegalArgumentException("A socket element must extend " + Supplier.class.getCanonicalName());
		}
		
		TypeMirror beanType = null;
		if(((DeclaredType)supplierType.get()).getTypeArguments().size() == 0) {
			beanType = this.processingEnvironment.getElementUtils().getTypeElement(Object.class.getCanonicalName()).asType();
		}
		else {
			beanType = ((DeclaredType)supplierType.get()).getTypeArguments().get(0);
		}
		
		// Socket name
		if(socketName == null || socketName.equals("")) {
			socketName = socketTypeElement.getSimpleName().toString();
			socketName = Character.toLowerCase(socketName.charAt(0)) + socketName.substring(1);
		}

		// This should never throw a QualifiedNameFormatException as it should have already been tested when the module was compiled
		BeanQualifiedName socketQName = new BeanQualifiedName(this.moduleQName, socketName);

		AnnotationMirror[] selectors = element.getAnnotationMirrors().stream().filter(a -> a.getAnnotationType().asElement().getAnnotation(Selector.class) != null).toArray(AnnotationMirror[]::new);
		
		MultiSocketType multiType = this.getMultiType(beanType);
		final AbstractSocketBeanInfo moduleSocketInfo;
		// Use compiledModuleElement instead of moduleElement to report compilation errors on the compiled module 
		if(multiType != null) {
			moduleSocketInfo = new CommonMultiSocketBeanInfo(this.processingEnvironment, this.compiledModuleElement, socketQName, this.getComponentType(beanType), socketType, socketElement, selectors, optional, multiType);
		}
		else {
			moduleSocketInfo = new CommonSingleSocketBeanInfo(this.processingEnvironment, this.compiledModuleElement, socketQName, beanType, socketType, socketElement, selectors, optional);
		}
		
		return moduleSocketInfo;
	}
	
	private AbstractSocketBeanInfo createConfigurationSocketBean(DeclaredType socketType, ExecutableElement socketElement) throws SocketCompilationException {
		TypeElement configElement = (TypeElement)this.processingEnvironment.getTypeUtils().asElement(socketType.getTypeArguments().get(0));
		
		if(configElement.getKind() != ElementKind.INTERFACE) {
			throw new IllegalArgumentException("A configuration element must be an interface");
		}
		
		AnnotationMirror beanAnnotation = null;
		AnnotationMirror configurationAnnotation = null;
		for(AnnotationMirror annotation : configElement.getAnnotationMirrors()) {
			if(this.processingEnvironment.getTypeUtils().isSameType(annotation.getAnnotationType(), this.beanAnnotationType)) {
				beanAnnotation = annotation;
			}
			else if(this.processingEnvironment.getTypeUtils().isSameType(annotation.getAnnotationType(), this.configurationAnnotationType)) {
				configurationAnnotation = annotation;
			}
		}
		if(beanAnnotation == null) {
			throw new IllegalArgumentException("The specified element is not annotated with " + Bean.class.getSimpleName());
		}
		if(configurationAnnotation == null) {
			throw new IllegalArgumentException("The specified element is not annotated with " + Configuration.class.getSimpleName());
		}
		
		String socketName = null;
		Bean.Visibility visibility = null;
		for(Entry<? extends ExecutableElement, ? extends AnnotationValue> value : this.processingEnvironment.getElementUtils().getElementValuesWithDefaults(beanAnnotation).entrySet()) {
			switch(value.getKey().getSimpleName().toString()) {
				case "name" : socketName = (String)value.getValue().getValue();
					break;
				case "visibility" : visibility = Bean.Visibility.valueOf(value.getValue().getValue().toString());
					break;
			}
		}
		
		if(visibility.equals(Bean.Visibility.PRIVATE)) {
			throw new IllegalStateException("A configuration bean must be public");
		}
		
		TypeMirror beanType = configElement.asType();
		
		// Socket name
		if(socketName == null || socketName.equals("")) {
			socketName = configElement.getSimpleName().toString();
			socketName = Character.toLowerCase(socketName.charAt(0)) + socketName.substring(1);
		}

		// This should never throw a QualifiedNameFormatException as it should have already been tested when the module was compiled
		BeanQualifiedName socketQName = new BeanQualifiedName(this.moduleQName, socketName);
		
		BinaryConfigurationSocketBeanInfo configurationSocketBeanInfo = new BinaryConfigurationSocketBeanInfo(this.processingEnvironment, this.compiledModuleElement, socketQName, beanType, socketType, socketElement);
		
		return configurationSocketBeanInfo;
	}
}
