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

import io.winterframework.core.annotation.Selector;
import io.winterframework.core.compiler.WinterCompiler;
import io.winterframework.core.compiler.spi.BeanQualifiedName;
import io.winterframework.core.compiler.spi.MultiSocketType;
import io.winterframework.core.compiler.spi.SocketBeanInfo;

/**
 * <p>
 * A {@link SocketBeanInfoFactory} implementation used by the
 * {@link WinterCompiler} to create {@link SocketBeanInfo} for
 * component modules required in other modules (possibly compiled modules).
 * </p>
 * 
 * @author <a href="mailto:jeremy.kuhn@winterframework.io">Jeremy Kuhn</a>
 *
 */
class BinarySocketBeanInfoFactory extends SocketBeanInfoFactory {

	private ModuleElement compiledModuleElement;
	
	private TypeMirror supplierType;
	private TypeMirror optionalType;
	
	private TypeMirror socketAnnotationType;
	
	/**
	 * @param processingEnvironment
	 * @param moduleElement
	 */
	public BinarySocketBeanInfoFactory(ProcessingEnvironment processingEnvironment, ModuleElement moduleElement, ModuleElement compiledModuleElement) {
		super(processingEnvironment, moduleElement);
		
		this.compiledModuleElement = compiledModuleElement;
		this.supplierType = this.processingEnvironment.getTypeUtils().erasure(this.processingEnvironment.getElementUtils().getTypeElement(Supplier.class.getCanonicalName()).asType());
		this.optionalType = this.processingEnvironment.getTypeUtils().erasure(this.processingEnvironment.getElementUtils().getTypeElement(Optional.class.getCanonicalName()).asType());
		this.socketAnnotationType = this.processingEnvironment.getElementUtils().getTypeElement("io.winterframework.core.v1.Module.Socket").asType();
	}

	@SuppressWarnings("unchecked")
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
		boolean optional = false;
		if(this.processingEnvironment.getTypeUtils().isSameType(this.processingEnvironment.getTypeUtils().erasure(socketType), this.optionalType)) {
			optional = true;
			socketType = ((DeclaredType)socketType).getTypeArguments().get(0);
		}
		
		TypeElement socketTypeElement = (TypeElement)this.processingEnvironment.getTypeUtils().asElement(socketType);
		
		if(socketTypeElement.getKind() != ElementKind.INTERFACE) {
			throw new IllegalArgumentException("A socket bean must be an interface");
		}
		
		Optional<? extends AnnotationMirror> socketAnnotation = variableElement.getAnnotationMirrors().stream().filter(a -> this.processingEnvironment.getTypeUtils().isSameType(a.getAnnotationType(), this.socketAnnotationType)).findFirst();
		if(!socketAnnotation.isPresent()) {
			throw new IllegalArgumentException("The specified element does not provide socket information");
		}
		
		String socketName = null;
		Set<BeanQualifiedName> wiredBeanQNames = Set.of();
		for(Entry<? extends ExecutableElement, ? extends AnnotationValue> value : this.processingEnvironment.getElementUtils().getElementValuesWithDefaults(socketAnnotation.get()).entrySet()) {
			switch(value.getKey().getSimpleName().toString()) {
				case "name" : socketName = (String)value.getValue().getValue();
					break;
				case "wiredTo" : wiredBeanQNames = ((List<AnnotationValue>)value.getValue().getValue()).stream().map(v -> (String)v.getValue()).map(name -> new BeanQualifiedName(this.moduleQName, name)).collect(Collectors.toSet());
					break;
			}
		}
		
		DeclaredType supplierType;
		if(this.processingEnvironment.getTypeUtils().isSameType(this.processingEnvironment.getTypeUtils().erasure(socketType), this.supplierType)) {
			supplierType = (DeclaredType)socketType;
			if(socketName == null || socketName.equals("")) {
				throw new IllegalArgumentException("A socket bean specified as a " + Supplier.class.getCanonicalName() + " must provide an explicit name");
			}
		}
		else {
			Optional<? extends TypeMirror> supplierTypeInterface = socketTypeElement.getInterfaces().stream().filter(t -> this.processingEnvironment.getTypeUtils().isSameType(this.processingEnvironment.getTypeUtils().erasure(t), this.supplierType)).findFirst();
			if(!supplierTypeInterface.isPresent()) {
				throw new IllegalArgumentException("A socket bean must be a " + Supplier.class.getCanonicalName() + " or directly extend " + Supplier.class.getCanonicalName());
			}
			supplierType = (DeclaredType)supplierTypeInterface.get();
		}
		
		TypeMirror beanType = null;
		if(supplierType.getTypeArguments().size() == 0) {
			beanType = this.processingEnvironment.getElementUtils().getTypeElement(Object.class.getCanonicalName()).asType();
		}
		else {
			beanType = supplierType.getTypeArguments().get(0);
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
		
		// A binary socket is always wired
		moduleSocketInfo.setWired(true);
		moduleSocketInfo.setWiredBeans(wiredBeanQNames);
		
		return moduleSocketInfo;
	}
}
