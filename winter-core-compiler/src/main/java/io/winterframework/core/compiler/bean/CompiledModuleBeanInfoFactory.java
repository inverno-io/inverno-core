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
package io.winterframework.core.compiler.bean;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.ModuleElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;
import javax.tools.Diagnostic.Kind;

import io.winterframework.core.annotation.Bean;
import io.winterframework.core.annotation.BeanSocket;
import io.winterframework.core.annotation.Destroy;
import io.winterframework.core.annotation.Init;
import io.winterframework.core.annotation.Provide;
import io.winterframework.core.annotation.Wrapper;
import io.winterframework.core.compiler.ModuleAnnotationProcessor;
import io.winterframework.core.compiler.TypeErrorException;
import io.winterframework.core.compiler.common.ReporterInfo;
import io.winterframework.core.compiler.spi.BeanQualifiedName;
import io.winterframework.core.compiler.spi.BeanSocketQualifiedName;
import io.winterframework.core.compiler.spi.ModuleBeanInfo;
import io.winterframework.core.compiler.spi.ModuleBeanSocketInfo;
import io.winterframework.core.compiler.spi.QualifiedNameFormatException;

/**
 * <p>
 * A {@link ModuleBeanInfoFactory} implementation used by the
 * {@link ModuleAnnotationProcessor} to create {@link ModuleBeanInfo} for
 * modules being compiled.
 * </p>
 * 
 * @author jkuhn
 *
 */
class CompiledModuleBeanInfoFactory extends ModuleBeanInfoFactory {

	private TypeMirror beanAnnotationType;
	private TypeMirror provideAnnotationType;
	private TypeMirror wrapperAnnotationType;
	private TypeMirror supplierType;
	
	private NestedBeanInfoFactory nestedBeanFactory;
	
	/**
	 * @param processingEnvironment
	 * @param moduleElement
	 */
	CompiledModuleBeanInfoFactory(ProcessingEnvironment processingEnvironment, ModuleElement moduleElement) {
		super(processingEnvironment, moduleElement);
		
		this.beanAnnotationType = this.processingEnvironment.getElementUtils().getTypeElement(Bean.class.getCanonicalName()).asType();
		this.provideAnnotationType = this.processingEnvironment.getElementUtils().getTypeElement(Provide.class.getCanonicalName()).asType();
		this.wrapperAnnotationType = this.processingEnvironment.getElementUtils().getTypeElement(Wrapper.class.getCanonicalName()).asType();
		this.supplierType = this.processingEnvironment.getTypeUtils().erasure(this.processingEnvironment.getElementUtils().getTypeElement(Supplier.class.getCanonicalName()).asType());
		
		this.nestedBeanFactory = new NestedBeanInfoFactory(this.processingEnvironment);
	}

	@Override
	public ModuleBeanInfo createBean(Element element) throws BeanCompilationException {
		if(!TypeElement.class.isAssignableFrom(element.getClass())) {
			throw new IllegalArgumentException("Element must be a TypeElement");
		}
		if(!element.getKind().equals(ElementKind.CLASS)) {
			throw new IllegalArgumentException("Element must be a Class");
		}
		
		TypeElement typeElement = (TypeElement)element;
		
		for(Element moduleElement = typeElement; moduleElement != null;moduleElement = moduleElement.getEnclosingElement()) {
			if(moduleElement instanceof ModuleElement && !moduleElement.equals(this.moduleElement)) {
				throw new IllegalArgumentException("The specified element doesn't belong to module " + this.moduleQName);
			}
		}
		
		/*if(!typeElement.getEnclosingElement().getEnclosingElement().equals(this.moduleElement)) {
			throw new IllegalArgumentException("The specified element doesn't belong to module " + this.moduleQName);
		}*/
		
		Optional<? extends AnnotationMirror> beanAnnotation = this.processingEnvironment.getElementUtils().getAllAnnotationMirrors(typeElement).stream().filter(a -> this.processingEnvironment.getTypeUtils().isSameType(a.getAnnotationType(), this.beanAnnotationType)).findFirst();
		if(!beanAnnotation.isPresent()) {
			throw new IllegalArgumentException("The specified element is not annotated with " + Bean.class.getSimpleName());
		}
		
		ReporterInfo beanReporter = this.getReporter(typeElement, beanAnnotation.get());
		if(typeElement.getModifiers().contains(Modifier.ABSTRACT)) {
			beanReporter.error("A bean must be a concrete class");
			throw new BeanCompilationException();
		}
		
		// Get Bean metadata
		String name = null;
		Bean.Visibility visibility = null;
		Bean.Strategy strategy = null;
		for(Entry<? extends ExecutableElement, ? extends AnnotationValue> value : this.processingEnvironment.getElementUtils().getElementValuesWithDefaults(beanAnnotation.get()).entrySet()) {
			switch(value.getKey().getSimpleName().toString()) {
				case "name" : name = (String)value.getValue().getValue();
					break;
				case "visibility" : visibility = Bean.Visibility.valueOf(value.getValue().getValue().toString());
					break;
				case "strategy" : strategy = Bean.Strategy.valueOf(value.getValue().getValue().toString());
					break;
			}
		}
		
		// Bean qualified name
		if(name == null || name.equals("")) {
			name = typeElement.getSimpleName().toString();
			name = Character.toLowerCase(name.charAt(0)) + name.substring(1);
		}
		
		BeanQualifiedName beanQName;
		try {
			beanQName = new BeanQualifiedName(this.moduleQName, name);
		} 
		catch (QualifiedNameFormatException e) {
			beanReporter.error("Invalid bean qualified name: " + e.getMessage());
			throw new BeanCompilationException();
		}

		TypeMirror beanType = typeElement.asType();
		
		// Get provided type
		TypeMirror providedType = null;
		List<? extends TypeMirror> providedTypes = this.processingEnvironment.getTypeUtils().directSupertypes(beanType).stream()
			.filter(superType -> superType.getAnnotationMirrors().stream().anyMatch(a -> this.processingEnvironment.getTypeUtils().isSameType(a.getAnnotationType(), this.provideAnnotationType)))
			.map(superType -> {
				TypeMirror[] superTypeArguments = ((DeclaredType)superType).getTypeArguments().stream().toArray(TypeMirror[]::new);
				TypeElement superTypeElement = (TypeElement)((DeclaredType)this.processingEnvironment.getTypeUtils().erasure(superType)).asElement();
				
				return this.processingEnvironment.getTypeUtils().getDeclaredType(superTypeElement, superTypeArguments);
			})
			.collect(Collectors.toList());
		
		if(providedTypes.size() == 1) {
			providedType = providedTypes.get(0);
		}
		else if(providedTypes.size() > 1) {
			beanReporter.error("Bean " + beanQName + " can't provide multiple types");
		}
		
		// Get Init
		List<ExecutableElement> initElements = typeElement.getEnclosedElements().stream()
			.filter(e -> e.getAnnotation(Init.class) != null)
			.map(e -> (ExecutableElement)e)
			.filter(e -> {
				if(((ExecutableElement)e).getParameters().size() > 0) {
					this.processingEnvironment.getMessager().printMessage(Kind.MANDATORY_WARNING, "Invalid " + Init.class.getSimpleName() + " method which should be a no-argument method, it will be ignored", e);
					return false;
				}
				return true;
			}).collect(Collectors.toList());
		
		// Get Destroy
		List<ExecutableElement> destroyElements = typeElement.getEnclosedElements().stream()
			.filter(e -> e.getAnnotation(Destroy.class) != null)
			.map(e -> (ExecutableElement)e)
			.filter(e -> {
				if(e.getParameters().size() > 0) {
					this.processingEnvironment.getMessager().printMessage(Kind.MANDATORY_WARNING, "Invalid " + Destroy.class.getSimpleName() + " method which should be a no-argument method, it will be ignored", e);
					return false;
				}
				return true;
			}).collect(Collectors.toList());
		
		// Get sockets...
		List<ModuleBeanSocketInfo> beanSocketInfos = new ArrayList<>();
		Map<String, ModuleBeanSocketInfo> requiredSocketByName = new HashMap<>();
		ModuleBeanSocketInfoFactory beanSocketFactory = ModuleBeanSocketInfoFactory.create(this.processingEnvironment, this.moduleElement, beanQName);
		
		// ... from Constructor
		ExecutableElement constructorSocketElement = null;
		
		List<ExecutableElement> constructorSocketElements = typeElement.getEnclosedElements().stream()
			.filter(e -> e.getKind().equals(ElementKind.CONSTRUCTOR))
			.filter(e -> e.getModifiers().stream().anyMatch(m -> m.equals(Modifier.PUBLIC)))
			.map(e -> (ExecutableElement)e)
			.collect(Collectors.toList());
		if(constructorSocketElements.size() == 0) {
			// This should never happen
			beanReporter.error("No public constructor defined in bean " + beanQName);
		}
		else if(constructorSocketElements.size() == 1) {
			// OK
			constructorSocketElement = constructorSocketElements.get(0);
		}
		else {
			// multiple constructor
			constructorSocketElements = constructorSocketElements.stream().filter(e -> e.getAnnotation(BeanSocket.class) != null).collect(Collectors.toList());
			
			if(constructorSocketElements.size() == 0) {
				beanReporter.error("Multiple constructors are defined in module bean " + beanQName + ", consider specifying a " + BeanSocket.class.getSimpleName() + " on the one to select");
			}
			else if(constructorSocketElements.size() == 1) {
				constructorSocketElement = constructorSocketElements.get(0);
			}
			else {
				beanReporter.error("Multiple constructors annotated with " + BeanSocket.class.getSimpleName() + " are defined in module bean " + beanQName + " which is not permitted");
			}
		}
		
		if(constructorSocketElement != null) {
			for(VariableElement ve : constructorSocketElement.getParameters()) {
				try {
					beanSocketFactory.createBeanSocket(ve).ifPresent(requiredBeanSocket ->  {
						requiredSocketByName.put(requiredBeanSocket.getQualifiedName().getName(), requiredBeanSocket);
						beanSocketInfos.add(requiredBeanSocket);
					});
				} 
				catch (TypeErrorException e1) {
					beanReporter.error("Invalid required socket " + ve.getSimpleName().toString() + " : Type " + e1.getType() + " could not be resolved");
				}
			}
		}
		
		// ... from setters
		// Get optional dependencies from public setters
		List<ExecutableElement> optionalSocketElements = (List<ExecutableElement>)typeElement.getEnclosedElements().stream()
			.filter(e -> e.getKind().equals(ElementKind.METHOD) && e.getSimpleName().toString().startsWith("set"))
			.filter(e -> e.getModifiers().stream().anyMatch(m -> m.equals(Modifier.PUBLIC)))
			.map(e -> (ExecutableElement)e)
			.collect(Collectors.toList());
		List<ExecutableElement> annotatedSocketElements = optionalSocketElements.stream().filter(e -> e.getAnnotation(BeanSocket.class) != null).collect(Collectors.toList());
		if(annotatedSocketElements.size() > 0) {
			optionalSocketElements = annotatedSocketElements;
		}

		Predicate<ModuleBeanSocketInfo> requiredSocketConflictPredicate = optionalBeanSocketInfo -> {
			if(requiredSocketByName.containsKey(optionalBeanSocketInfo.getQualifiedName().getName())) {
				requiredSocketByName.get(optionalBeanSocketInfo.getQualifiedName().getName()).error("Required socket name is conflicting with an optional socket: " + optionalBeanSocketInfo.getQualifiedName().getName());
				optionalBeanSocketInfo.error("Optional socket name is conflicting with a required socket: " + optionalBeanSocketInfo.getQualifiedName().getName());
				return false;
			}
			return true;
		};
		for(List<ExecutableElement> socketElementsBySocketName : optionalSocketElements.stream().collect(Collectors.groupingBy(ExecutableElement::getSimpleName)).values()) {
			if(socketElementsBySocketName.size() > 1) {
				List<ModuleBeanSocketInfo> optionalModuleSocketInfos = new ArrayList<>();
				for(ExecutableElement socketElement : socketElementsBySocketName) {
					try {
						beanSocketFactory.createBeanSocket(socketElement.getParameters().get(0)).ifPresent(optionalModuleSocketInfos::add);
					} 
					catch (TypeErrorException e1) {
						this.processingEnvironment.getMessager().printMessage(Kind.MANDATORY_WARNING, "Ignoring invalid optional socket: Type " + e1.getType() + " could not be resolved", socketElement);
					}
				}
				Map<BeanSocketQualifiedName, List<ModuleBeanSocketInfo>> socketInfosByName = optionalModuleSocketInfos.stream().collect(Collectors.groupingBy(ModuleBeanSocketInfo::getQualifiedName));
				socketInfosByName.values().stream()
					.filter(socketInfos -> socketInfos.size() > 1)
					.forEach(socketInfos -> {
						socketInfos.forEach(socketInfo -> {
							socketInfo.error("Optional socket name is conflicting with another optional socket: " + socketInfo.getQualifiedName().getName());
						});
					});
				
				socketInfosByName.values().stream()
					.filter(socketInfos -> socketInfos.size() == 1)
					.map(socketInfos -> socketInfos.get(0))
					.filter(requiredSocketConflictPredicate)
					.forEach(beanSocketInfos::add);
			}
			else {
				try {
					beanSocketFactory.createBeanSocket(socketElementsBySocketName.get(0).getParameters().get(0))
						.filter(requiredSocketConflictPredicate)
						.ifPresent(beanSocketInfos::add);
				}
				catch (TypeErrorException e1) {
					this.processingEnvironment.getMessager().printMessage(Kind.MANDATORY_WARNING, "Ignoring invalid optional socket: Type " + e1.getType() + " could not be resolved", socketElementsBySocketName.get(0));
				}
			}
		}
		
		Optional<? extends AnnotationMirror> wrapperAnnotation = this.processingEnvironment.getElementUtils().getAllAnnotationMirrors(typeElement).stream().filter(a -> this.processingEnvironment.getTypeUtils().isSameType(a.getAnnotationType(), this.wrapperAnnotationType)).findFirst();
		TypeMirror wrapperType = null;
		CommonModuleBeanInfo moduleBeanInfo;
		if(wrapperAnnotation.isPresent()) {
			wrapperType = beanType;
			Optional<? extends TypeMirror> supplierType = typeElement.getInterfaces().stream().filter(t -> this.processingEnvironment.getTypeUtils().isSameType(this.processingEnvironment.getTypeUtils().erasure(t), this.supplierType)).findFirst();
			if(supplierType.isPresent()) {
				if(((DeclaredType)supplierType.get()).getTypeArguments().size() == 0) {
					beanType = this.processingEnvironment.getElementUtils().getTypeElement(Object.class.getCanonicalName()).asType();
				}
				else {
					beanType = ((DeclaredType)supplierType.get()).getTypeArguments().get(0);
				}
			}
			else {
				beanReporter.error("A wrapper bean element must extend " + Supplier.class.getCanonicalName());
			}
			if(providedType != null) {
				beanReporter.error("Wrapper bean " + beanQName + " can't provide other types than its supplied type");
			}
			
			if(beanReporter.hasError()) {
				throw new BeanCompilationException();
			}
			moduleBeanInfo = new CompiledWrapperBeanInfo(this.processingEnvironment, typeElement, beanAnnotation.get(), beanQName, wrapperType, beanType, visibility, strategy, initElements, destroyElements, beanSocketInfos);
		}
		else {
			if(beanReporter.hasError()) {
				throw new BeanCompilationException();
			}
			moduleBeanInfo = new CommonModuleBeanInfo(this.processingEnvironment, typeElement, beanAnnotation.get(), beanQName, beanType, providedType, visibility, strategy, initElements, destroyElements, beanSocketInfos);
		}
		
		// Get Nested Beans
		moduleBeanInfo.setNestedBeanInfos(this.nestedBeanFactory.create(moduleBeanInfo));
		
		return moduleBeanInfo;
	}
}
