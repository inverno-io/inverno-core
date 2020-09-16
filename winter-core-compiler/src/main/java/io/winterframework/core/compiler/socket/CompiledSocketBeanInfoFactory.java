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

import java.util.Map.Entry;
import java.util.List;
import java.util.Optional;
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
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.tools.Diagnostic.Kind;

import io.winterframework.core.annotation.Bean;
import io.winterframework.core.annotation.NestedBean;
import io.winterframework.core.annotation.Selector;
import io.winterframework.core.compiler.ModuleAnnotationProcessor;
import io.winterframework.core.compiler.TypeErrorException;
import io.winterframework.core.compiler.common.CompiledNestedBeanInfo;
import io.winterframework.core.compiler.common.ReporterInfo;
import io.winterframework.core.compiler.spi.BeanInfo;
import io.winterframework.core.compiler.spi.BeanQualifiedName;
import io.winterframework.core.compiler.spi.MultiSocketType;
import io.winterframework.core.compiler.spi.NestedBeanInfo;
import io.winterframework.core.compiler.spi.QualifiedNameFormatException;
import io.winterframework.core.compiler.spi.SocketBeanInfo;

/**
 * <p>
 * A {@link SocketBeanInfoFactory} implementation used by the
 * {@link ModuleAnnotationProcessor} to create {@link SocketBeanInfo} for
 * modules being compiled.
 * </p>
 * 
 * @author jkuhn
 *
 */
class CompiledSocketBeanInfoFactory extends SocketBeanInfoFactory {

	private TypeMirror beanAnnotationType;
	private TypeMirror supplierType;
	
	public CompiledSocketBeanInfoFactory(ProcessingEnvironment processingEnvironment, ModuleElement moduleElement) {
		super(processingEnvironment, moduleElement);
		
		this.beanAnnotationType = this.processingEnvironment.getElementUtils().getTypeElement(Bean.class.getCanonicalName()).asType();
		this.supplierType = this.processingEnvironment.getTypeUtils().erasure(this.processingEnvironment.getElementUtils().getTypeElement(Supplier.class.getCanonicalName()).asType());
	}

	@Override
	public WirableSocketBeanInfo createSocketBean(Element element) throws SocketCompilationException {
		if(!TypeElement.class.isAssignableFrom(element.getClass())) {
			throw new IllegalArgumentException("Element must be a TypeElement");
		}
		
		TypeElement typeElement = (TypeElement)element;
		
		for(Element moduleElement = element; moduleElement != null;moduleElement = moduleElement.getEnclosingElement()) {
			if(moduleElement instanceof ModuleElement && !moduleElement.equals(this.moduleElement)) {
				throw new IllegalArgumentException("The specified element doesn't belong to module " + this.moduleQName);
			}
		}
		
		/*if(!typeElement.getEnclosingElement().getEnclosingElement().equals(this.moduleElement)) {
			throw new IllegalArgumentException("The specified element doesn't belong to module " + this.moduleQName);
		}*/
		
		Optional<? extends AnnotationMirror> annotation = this.processingEnvironment.getElementUtils().getAllAnnotationMirrors(element).stream().filter(a -> this.processingEnvironment.getTypeUtils().isSameType(a.getAnnotationType(), this.beanAnnotationType)).findFirst();
		if(!annotation.isPresent()) {
			throw new IllegalArgumentException("The specified element is not annotated with " + Bean.class.getSimpleName());
		}
		
		ReporterInfo beanReporter = this.getReporter(element, annotation.get());
		
		if(!element.getKind().equals(ElementKind.INTERFACE)) {
			// This should never happen, we shouldn't get there if it wasn't an interface
			beanReporter.error("A socket bean must be an interface");
			throw new SocketCompilationException();
		}
		
		Optional<? extends TypeMirror> supplierType = typeElement.getInterfaces().stream().filter(t -> this.processingEnvironment.getTypeUtils().isSameType(this.processingEnvironment.getTypeUtils().erasure(t), this.supplierType)).findFirst();
		if(!supplierType.isPresent()) {
			beanReporter.error("A socket bean must extend " + Supplier.class.getCanonicalName());
			throw new SocketCompilationException();
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
		
		if(!visibility.equals(Bean.Visibility.PUBLIC)) {
			beanReporter.error("A socket bean must be public");
			throw new SocketCompilationException();
		}
		
		TypeMirror socketType = null;
		if(((DeclaredType)supplierType.get()).getTypeArguments().size() == 0) {
			socketType = this.processingEnvironment.getElementUtils().getTypeElement(Object.class.getCanonicalName()).asType();
		}
		else {
			socketType = ((DeclaredType)supplierType.get()).getTypeArguments().get(0);
		}
		
		try {
			this.validateType(socketType);
		} 
		catch (TypeErrorException e1) {
			this.processingEnvironment.getMessager().printMessage(Kind.WARNING, "Type " + e1.getType() + " could not be resolved.", element);
		}
		
		// Socket name
		if(socketName == null || socketName.equals("")) {
			socketName = element.getSimpleName().toString();
			socketName = Character.toLowerCase(socketName.charAt(0)) + socketName.substring(1);
		}

		BeanQualifiedName socketQName = null;
		try {
			socketQName = new BeanQualifiedName(this.moduleQName, socketName);
		} catch (QualifiedNameFormatException e) {
			beanReporter.error("Invalid socket bean qualified name: " + e.getMessage());
			throw new SocketCompilationException();
		}
		
		AnnotationMirror[] selectors = element.getAnnotationMirrors().stream().filter(a -> a.getAnnotationType().asElement().getAnnotation(Selector.class) != null).toArray(AnnotationMirror[]::new);

		// Optional or non-optional will be resolved during wiring {@see io.winterframework.core.compiler.wire.SocketResolver}
		MultiSocketType multiType = this.getMultiType(socketType);
		if(multiType != null) {
			return new CommonMultiSocketBeanInfo(this.processingEnvironment, typeElement, annotation.get(), socketQName, this.getComponentType(socketType), typeElement.asType(), selectors, true, multiType);
		}
		else {
			CommonSingleSocketBeanInfo socketBeanInfo = new CommonSingleSocketBeanInfo(this.processingEnvironment, typeElement, annotation.get(), socketQName, socketType, typeElement.asType(), selectors, true);
			socketBeanInfo.setNestedBeans(this.extractNestedBeans(typeElement, socketBeanInfo));
			return socketBeanInfo;
		}
	}
	
	// TODO this doesn't seem to support generics properly
	// TODO we could also report some error on the socket when a nested bean is not declared properly
	private List<NestedBeanInfo> extractNestedBeans(Element element, BeanInfo providingBean) {
		if(!TypeElement.class.isAssignableFrom(element.getClass())) {
			return List.of();
		}
		return element.getEnclosedElements().stream()
			.filter(e -> e.getAnnotation(NestedBean.class) != null)
			.map(e -> (ExecutableElement)e)
			.filter(e -> e.getParameters().size() > 0 && e.getReturnType().getKind().equals(TypeKind.VOID))
			.map(e -> {
				CompiledNestedBeanInfo nestedBeanInfo = new CompiledNestedBeanInfo(this.processingEnvironment, e, providingBean);
				nestedBeanInfo.setNestedBeans(this.extractNestedBeans(this.processingEnvironment.getTypeUtils().asElement(e.getReturnType()), nestedBeanInfo));
				return nestedBeanInfo;
			})
			.collect(Collectors.toList());
	}
}
