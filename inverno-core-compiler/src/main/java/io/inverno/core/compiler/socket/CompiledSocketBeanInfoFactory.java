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
package io.inverno.core.compiler.socket;

import java.util.Map.Entry;
import java.util.Optional;
import java.util.function.Supplier;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.ModuleElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;
import javax.tools.Diagnostic.Kind;

import io.inverno.core.annotation.Bean;
import io.inverno.core.annotation.Selector;
import io.inverno.core.compiler.InvernoCompiler;
import io.inverno.core.compiler.TypeErrorException;
import io.inverno.core.compiler.bean.NestedBeanInfoFactory;
import io.inverno.core.compiler.spi.BeanQualifiedName;
import io.inverno.core.compiler.spi.MultiSocketType;
import io.inverno.core.compiler.spi.QualifiedNameFormatException;
import io.inverno.core.compiler.spi.ReporterInfo;
import io.inverno.core.compiler.spi.SocketBeanInfo;

/**
 * <p>
 * A {@link SocketBeanInfoFactory} implementation used by the {@link InvernoCompiler} to create {@link SocketBeanInfo} for modules being compiled.
 * </p>
 *
 * @author <a href="mailto:jeremy.kuhn@inverno.io">Jeremy Kuhn</a>
 *
 */
class CompiledSocketBeanInfoFactory extends SocketBeanInfoFactory {

	private final TypeMirror beanAnnotationType;
	private final TypeMirror supplierType;
	
	private final NestedBeanInfoFactory nestedBeanFactory;
	
	public CompiledSocketBeanInfoFactory(ProcessingEnvironment processingEnvironment, ModuleElement moduleElement) {
		super(processingEnvironment, moduleElement);
		
		this.beanAnnotationType = this.processingEnvironment.getElementUtils().getTypeElement(Bean.class.getCanonicalName()).asType();
		this.supplierType = this.processingEnvironment.getTypeUtils().erasure(this.processingEnvironment.getElementUtils().getTypeElement(Supplier.class.getCanonicalName()).asType());
		
		this.nestedBeanFactory = new NestedBeanInfoFactory(this.processingEnvironment);
	}

	@Override
	public WirableSocketBeanInfo createSocketBean(Element element) throws SocketCompilationException {
		if(!TypeElement.class.isAssignableFrom(element.getClass())) {
			throw new IllegalArgumentException("Element must be a TypeElement");
		}
		
		TypeElement typeElement = (TypeElement)element;
		
		for(Element currentModuleElement = element; currentModuleElement != null;currentModuleElement = currentModuleElement.getEnclosingElement()) {
			if(currentModuleElement instanceof ModuleElement && !currentModuleElement.equals(this.moduleElement)) {
				throw new IllegalArgumentException("The specified element doesn't belong to module " + this.moduleQName);
			}
		}
		
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
		
		Optional<? extends TypeMirror> beanSupplierType = typeElement.getInterfaces().stream().filter(t -> this.processingEnvironment.getTypeUtils().isSameType(this.processingEnvironment.getTypeUtils().erasure(t), this.supplierType)).findFirst();
		if(!beanSupplierType.isPresent()) {
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
		
		TypeMirror socketType;
		if(((DeclaredType)beanSupplierType.get()).getTypeArguments().isEmpty()) {
			socketType = this.processingEnvironment.getElementUtils().getTypeElement(Object.class.getCanonicalName()).asType();
		}
		else {
			socketType = ((DeclaredType)beanSupplierType.get()).getTypeArguments().get(0);
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

		// Optional or non-optional will be resolved during wiring {@see io.inverno.core.compiler.wire.SocketResolver}
		MultiSocketType multiType = this.getMultiType(socketType);
		if(multiType != null) {
			return new CommonMultiSocketBeanInfo(this.processingEnvironment, typeElement, annotation.get(), socketQName, this.getComponentType(socketType), typeElement.asType(), selectors, true, multiType);
		}
		else {
			CommonSingleSocketBeanInfo socketBeanInfo = new CommonSingleSocketBeanInfo(this.processingEnvironment, typeElement, annotation.get(), socketQName, socketType, typeElement.asType(), selectors, true);
			socketBeanInfo.setNestedBeans(this.nestedBeanFactory.create(socketBeanInfo));
			return socketBeanInfo;
		}
	}
}
