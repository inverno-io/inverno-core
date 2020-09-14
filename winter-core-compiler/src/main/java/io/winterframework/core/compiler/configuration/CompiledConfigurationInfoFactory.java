/*
 * Copyright 2020 Jeremy KUHN
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
package io.winterframework.core.compiler.configuration;

import java.util.List;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.ModuleElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.tools.Diagnostic.Kind;

import io.winterframework.core.annotation.Bean;
import io.winterframework.core.annotation.Configuration;
import io.winterframework.core.compiler.ModuleAnnotationProcessor;
import io.winterframework.core.compiler.TypeErrorException;
import io.winterframework.core.compiler.bean.BeanCompilationException;
import io.winterframework.core.compiler.common.ReporterInfo;
import io.winterframework.core.compiler.spi.BeanQualifiedName;
import io.winterframework.core.compiler.spi.ConfigurationInfo;
import io.winterframework.core.compiler.spi.QualifiedNameFormatException;

/**
 * <p>
 * A {@link ConfigurationInfoFactory} implementation used by the
 * {@link ModuleAnnotationProcessor} to create {@link ConfigurationInfo} for
 * compiled modules.
 * </p>
 * 
 * @author jkuhn
 *
 */
class CompiledConfigurationInfoFactory extends ConfigurationInfoFactory {

	private TypeMirror beanAnnotationType;
	
	private TypeMirror configurationAnnotationType;
	
	public CompiledConfigurationInfoFactory(ProcessingEnvironment processingEnvironment, ModuleElement moduleElement) {
		super(processingEnvironment, moduleElement);
		
		this.beanAnnotationType = this.processingEnvironment.getElementUtils().getTypeElement(Bean.class.getCanonicalName()).asType();
		this.configurationAnnotationType = this.processingEnvironment.getElementUtils().getTypeElement(Configuration.class.getCanonicalName()).asType();
	}
	
	@Override
	public ConfigurationInfo createConfiguration(Element element) throws BeanCompilationException, TypeErrorException {
		if(!TypeElement.class.isAssignableFrom(element.getClass())) {
			throw new IllegalArgumentException("The specified element must be a TypeElement");
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
		
		AnnotationMirror beanAnnotation = null;
		AnnotationMirror configurationAnnotation = null;
		for(AnnotationMirror annotation : element.getAnnotationMirrors()) {
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
		
		ReporterInfo beanReporter = this.getReporter(element, beanAnnotation);
		
		if(!element.getKind().equals(ElementKind.INTERFACE)) {
			// This should never happen, we shouldn't get there if it wasn't an interface
			beanReporter.error("A configuration bean must be an interface");
			throw new BeanCompilationException();
		}

		// Get Bean metadata
		String name = null;
		Bean.Visibility visibility = null;
		Bean.Strategy strategy = null;
		for(Entry<? extends ExecutableElement, ? extends AnnotationValue> value : this.processingEnvironment.getElementUtils().getElementValuesWithDefaults(beanAnnotation).entrySet()) {
			switch(value.getKey().getSimpleName().toString()) {
				case "name" : name = (String)value.getValue().getValue();
					break;
				case "visibility" : visibility = Bean.Visibility.valueOf(value.getValue().getValue().toString());
					break;
				case "strategy" : strategy = Bean.Strategy.valueOf(value.getValue().getValue().toString());
					break;
			}
		}
		
		if(!visibility.equals(Bean.Visibility.PUBLIC)) {
			beanReporter.error("A configuration bean must be public");
		}
		if(!strategy.equals(Bean.Strategy.SINGLETON)) {
			beanReporter.error("A configuration bean must be a singleton bean");
		}
		
		// Bean qualified name
		if(name == null || name.equals("")) {
			name = element.getSimpleName().toString();
			name = Character.toLowerCase(name.charAt(0)) + name.substring(1);
		}
		
		BeanQualifiedName configurationQName;
		try {
			configurationQName = new BeanQualifiedName(this.moduleQName, name);
		} 
		catch (QualifiedNameFormatException e) {
			beanReporter.error("Invalid bean qualified name: " + e.getMessage());
			throw new BeanCompilationException();
		}

		TypeMirror configurationType = typeElement.asType();
		
		List<? extends CompiledConfigurationPropertyInfo> configurationProperties = this.processingEnvironment.getElementUtils().getAllMembers(typeElement).stream()
			.filter(el -> el.getKind().equals(ElementKind.METHOD) && !this.processingEnvironment.getTypeUtils().isSameType(this.processingEnvironment.getElementUtils().getTypeElement(Object.class.getCanonicalName()).asType(), el.getEnclosingElement().asType()))
			.map(el -> {
				ExecutableElement propertyMethod = (ExecutableElement)el;
				
				boolean invalid = false;
				if(propertyMethod.getParameters().size() > 0) {
					this.processingEnvironment.getMessager().printMessage(Kind.ERROR, "Configuration property must be declared as a no-argument method", propertyMethod.getParameters().get(0));
					invalid = true;
				}
				if(propertyMethod.getReturnType().getKind().equals(TypeKind.VOID)) {
					this.processingEnvironment.getMessager().printMessage(Kind.ERROR, "Configuration property must be declared as a non-void method", propertyMethod);
					invalid = true;
				}
				if(invalid) {
					return null;
				}
				
				if(this.isNestedConfiguration(propertyMethod)) {
					return new CompiledNestedConfigurationPropertyInfo(this.processingEnvironment, propertyMethod, configurationQName);
				}
				else {
					return new CompiledConfigurationPropertyInfo(this.processingEnvironment, propertyMethod);
				}
			})
			.collect(Collectors.toList());
		
		return new CompiledConfigurationInfo(this.processingEnvironment, typeElement, beanAnnotation, configurationQName, configurationType, configurationProperties);
	}
	
	private boolean isNestedConfiguration(ExecutableElement propertyMethod) {
		TypeMirror type = propertyMethod.getReturnType();
		if(type.getKind().equals(TypeKind.DECLARED)) {
			TypeElement typeElement = (TypeElement)this.processingEnvironment.getTypeUtils().asElement(type);
			
			return !this.processingEnvironment.getElementUtils().getModuleOf(typeElement).equals(this.moduleElement)
				&& typeElement.getAnnotationMirrors().stream().anyMatch(annotation -> this.processingEnvironment.getTypeUtils().isSameType(annotation.getAnnotationType(), this.configurationAnnotationType)) 
				&& typeElement.getAnnotationMirrors().stream().anyMatch(annotation -> this.processingEnvironment.getTypeUtils().isSameType(annotation.getAnnotationType(), this.beanAnnotationType));
		}
		else {
			// primitive, array...
			return false;
		}
	}
}
