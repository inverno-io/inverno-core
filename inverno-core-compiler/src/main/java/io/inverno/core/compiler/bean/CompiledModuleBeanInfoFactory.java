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
package io.inverno.core.compiler.bean;

import io.inverno.core.annotation.Bean;
import io.inverno.core.annotation.BeanSocket;
import io.inverno.core.annotation.Destroy;
import io.inverno.core.annotation.Init;
import io.inverno.core.annotation.Mutator;
import io.inverno.core.annotation.Overridable;
import io.inverno.core.annotation.Provide;
import io.inverno.core.annotation.Wrapper;
import io.inverno.core.compiler.InvernoCompiler;
import io.inverno.core.compiler.TypeErrorException;
import io.inverno.core.compiler.spi.BeanQualifiedName;
import io.inverno.core.compiler.spi.BeanSocketQualifiedName;
import io.inverno.core.compiler.spi.ModuleBeanInfo;
import io.inverno.core.compiler.spi.ModuleBeanSocketInfo;
import io.inverno.core.compiler.spi.QualifiedNameFormatException;
import io.inverno.core.compiler.spi.ReporterInfo;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
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
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;
import javax.tools.Diagnostic.Kind;

/**
 * <p>
 * A {@link ModuleBeanInfoFactory} implementation used by the {@link InvernoCompiler} to create {@link ModuleBeanInfo} for modules being compiled.
 * </p>
 *
 * @author <a href="mailto:jeremy.kuhn@inverno.io">Jeremy Kuhn</a>
 *
 */
class CompiledModuleBeanInfoFactory extends ModuleBeanInfoFactory {

	private final TypeMirror beanAnnotationType;
	private final TypeMirror beanSocketAnnotationType;
	private final TypeMirror provideAnnotationType;
	private final TypeMirror wrapperAnnotationType;
	private final TypeMirror overridableAnnotationType;
	private final TypeMirror mutatorAnnotationType;
	private final TypeMirror supplierType;
	private final TypeMirror functionType;
	
	private final NestedBeanInfoFactory nestedBeanFactory;
	
	/**
	 * @param processingEnvironment
	 * @param moduleElement
	 */
	CompiledModuleBeanInfoFactory(ProcessingEnvironment processingEnvironment, ModuleElement moduleElement) {
		super(processingEnvironment, moduleElement);
		
		this.beanAnnotationType = this.processingEnvironment.getElementUtils().getTypeElement(Bean.class.getCanonicalName()).asType();
		this.beanSocketAnnotationType = this.processingEnvironment.getElementUtils().getTypeElement(BeanSocket.class.getCanonicalName()).asType();
		this.provideAnnotationType = this.processingEnvironment.getElementUtils().getTypeElement(Provide.class.getCanonicalName()).asType();
		this.wrapperAnnotationType = this.processingEnvironment.getElementUtils().getTypeElement(Wrapper.class.getCanonicalName()).asType();
		this.overridableAnnotationType = this.processingEnvironment.getElementUtils().getTypeElement(Overridable.class.getCanonicalName()).asType();
		this.mutatorAnnotationType = this.processingEnvironment.getElementUtils().getTypeElement(Mutator.class.getCanonicalName()).asType();
		this.supplierType = this.processingEnvironment.getTypeUtils().erasure(this.processingEnvironment.getElementUtils().getTypeElement(Supplier.class.getCanonicalName()).asType());
		this.functionType = this.processingEnvironment.getTypeUtils().erasure(this.processingEnvironment.getElementUtils().getTypeElement(Function.class.getCanonicalName()).asType());
		
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
		
		for(Element currentModuleElement = typeElement; currentModuleElement != null;currentModuleElement = currentModuleElement.getEnclosingElement()) {
			if(currentModuleElement instanceof ModuleElement && !currentModuleElement.equals(this.moduleElement)) {
				throw new IllegalArgumentException("The specified element doesn't belong to module " + this.moduleQName);
			}
		}
		
		/*if(!typeElement.getEnclosingElement().getEnclosingElement().equals(this.moduleElement)) {
			throw new IllegalArgumentException("The specified element doesn't belong to module " + this.moduleQName);
		}*/
		
		Optional<? extends AnnotationMirror> beanAnnotation = typeElement.getAnnotationMirrors().stream().filter(a -> this.processingEnvironment.getTypeUtils().isSameType(a.getAnnotationType(), this.beanAnnotationType)).findFirst();
		if(!beanAnnotation.isPresent()) {
			throw new IllegalArgumentException("The specified element is not annotated with " + Bean.class.getSimpleName());
		}
		
		ReporterInfo beanReporter = this.getReporter(typeElement, beanAnnotation.get());
		if(typeElement.getModifiers().contains(Modifier.ABSTRACT)) {
			beanReporter.error("A bean must be a concrete class");
			throw new BeanCompilationException();
		}
		
		Optional<? extends AnnotationMirror> mutatorAnnotation = this.processingEnvironment.getElementUtils().getAllAnnotationMirrors(typeElement).stream().filter(a -> this.processingEnvironment.getTypeUtils().isSameType(a.getAnnotationType(), this.mutatorAnnotationType)).findFirst();
		Optional<? extends AnnotationMirror> wrapperAnnotation = this.processingEnvironment.getElementUtils().getAllAnnotationMirrors(typeElement).stream().filter(a -> this.processingEnvironment.getTypeUtils().isSameType(a.getAnnotationType(), this.wrapperAnnotationType)).findFirst();
		Optional<? extends AnnotationMirror> overridableAnnotation = this.processingEnvironment.getElementUtils().getAllAnnotationMirrors(typeElement).stream().filter(a -> this.processingEnvironment.getTypeUtils().isSameType(a.getAnnotationType(), this.overridableAnnotationType)).findFirst();
		
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
		
		if(mutatorAnnotation.isPresent()) {
			boolean error = false;
			if(wrapperAnnotation.isPresent()) {
				beanReporter.error("A mutator socket bean can't be a wrapper bean");
				error = true;
			}
			if(overridableAnnotation.isPresent()) {
				beanReporter.error("A mutator socket bean can't be an overridable bean");
				error = true;
			}
			
			if(!visibility.equals(Bean.Visibility.PUBLIC)) {
				beanReporter.error("A mutator socket bean must be public");
			}
			
			if(error) {
				throw new BeanCompilationException();
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

		// Get Init
		List<ExecutableElement> initElements = this.getInitMethods(typeElement);
		
		// Get Destroy
		List<ExecutableElement> destroyElements = this.getDestroyMethods(typeElement);

		// Get sockets...
		List<ModuleBeanSocketInfo> beanSocketInfos = new ArrayList<>();
		ModuleBeanSocketInfoFactory beanSocketFactory = ModuleBeanSocketInfoFactory.create(this.processingEnvironment, this.moduleElement, beanQName);
		
		// ... from Constructor
		List<ModuleBeanSocketInfo> requiredBeanSocketInfos = this.getRequiredBeanSocketInfos(typeElement, beanReporter, beanSocketFactory, beanQName);
		beanSocketInfos.addAll(requiredBeanSocketInfos);
		
		// ... from setters
		List<ModuleBeanSocketInfo> optionalBeanSocketInfos = this.getOptionalBeanSocketInfos(typeElement, beanSocketFactory, requiredBeanSocketInfos);
		beanSocketInfos.addAll(optionalBeanSocketInfos);
		
		CommonModuleBeanInfo moduleBeanInfo;
		if(mutatorAnnotation.isPresent()) {
			boolean required = false;
			for(Entry<? extends ExecutableElement, ? extends AnnotationValue> value : this.processingEnvironment.getElementUtils().getElementValuesWithDefaults(mutatorAnnotation.get()).entrySet()) {
				switch(value.getKey().getSimpleName().toString()) {
					case "required" : required = (boolean)value.getValue().getValue();
						break;
				}
			}
			TypeMirror mutatorType = typeElement.asType();
			TypeMirror socketType = mutatorType;
			TypeMirror beanType = mutatorType;
			Optional<? extends TypeMirror> beanFunctionType = typeElement.getInterfaces().stream().filter(t -> this.processingEnvironment.getTypeUtils().isSameType(this.processingEnvironment.getTypeUtils().erasure(t), this.functionType)).findFirst();
			if(beanFunctionType.isPresent()) {
				if(((DeclaredType)beanFunctionType.get()).getTypeArguments().isEmpty()) {
					socketType = beanType = this.processingEnvironment.getElementUtils().getTypeElement(Object.class.getCanonicalName()).asType();
				}
				else {
					socketType = ((DeclaredType)beanFunctionType.get()).getTypeArguments().get(0);
					beanType = ((DeclaredType)beanFunctionType.get()).getTypeArguments().get(1);
				}
			}
			else {
				beanReporter.error("A mutator socket bean element must extend " + Function.class.getCanonicalName());
			}

			TypeMirror providedType = this.getProvidedType(typeElement, beanReporter, beanQName, beanType);
			CompiledMutatorBeanInfo mutatorBeanInfo = new CompiledMutatorBeanInfo(this.processingEnvironment, typeElement, beanAnnotation.get(), beanQName, mutatorType, beanType, providedType, Bean.Visibility.PRIVATE, strategy, initElements, destroyElements, beanSocketInfos, required);
			mutatorBeanInfo.setMutatingSocket(beanSocketFactory.createMutatingSocketBean(beanQName, mutatorBeanInfo, socketType));
			
			moduleBeanInfo = mutatorBeanInfo;
		}
		else if(wrapperAnnotation.isPresent()) {
			TypeMirror wrapperType = typeElement.asType();
			TypeMirror beanType = wrapperType;
			Optional<? extends TypeMirror> beanSupplierType = typeElement.getInterfaces().stream().filter(t -> this.processingEnvironment.getTypeUtils().isSameType(this.processingEnvironment.getTypeUtils().erasure(t), this.supplierType)).findFirst();
			if(beanSupplierType.isPresent()) {
				if(((DeclaredType)beanSupplierType.get()).getTypeArguments().isEmpty()) {
					beanType = this.processingEnvironment.getElementUtils().getTypeElement(Object.class.getCanonicalName()).asType();
				}
				else {
					beanType = ((DeclaredType)beanSupplierType.get()).getTypeArguments().get(0);
				}
			}
			else {
				beanReporter.error("A wrapper bean element must extend " + Supplier.class.getCanonicalName());
			}

			TypeMirror providedType = this.getProvidedType(typeElement, beanReporter, beanQName, beanType);
			moduleBeanInfo = new CompiledWrapperBeanInfo(this.processingEnvironment, typeElement, beanAnnotation.get(), beanQName, wrapperType, beanType, providedType, visibility, strategy, initElements, destroyElements, beanSocketInfos);
		}
		else {
			TypeMirror providedType = this.getProvidedType(typeElement, beanReporter, beanQName, typeElement.asType());
			moduleBeanInfo = new CommonModuleBeanInfo(this.processingEnvironment, typeElement, beanAnnotation.get(), beanQName, typeElement.asType(), providedType, visibility, strategy, initElements, destroyElements, beanSocketInfos);
		}
		
		ModuleBeanInfo resultModuleBeanInfo = moduleBeanInfo;

		if(overridableAnnotation.isPresent()) {
			CompiledOverridingSocketBeanInfo socketInfo = new CompiledOverridingSocketBeanInfo(this.processingEnvironment, typeElement, overridableAnnotation.get(), moduleBeanInfo.getQualifiedName(), moduleBeanInfo.getProvidedType() != null ? moduleBeanInfo.getProvidedType() : moduleBeanInfo.getType());
			resultModuleBeanInfo = new CompiledOverridableBeanInfo(moduleBeanInfo, socketInfo);
		}

		// Get Nested Beans
		moduleBeanInfo.setNestedBeanInfos(this.nestedBeanFactory.create(resultModuleBeanInfo));
		
		if(beanReporter.hasError()) {
			throw new BeanCompilationException();
		}
		return resultModuleBeanInfo;
	}
	
	private TypeMirror getProvidedType(TypeElement typeElement, ReporterInfo beanReporter, BeanQualifiedName beanQName, TypeMirror beanType) throws BeanCompilationException {
		TypeMirror providedType = null;
		Optional<? extends AnnotationMirror> provideAnnotation = typeElement.getAnnotationMirrors().stream().filter(a -> this.processingEnvironment.getTypeUtils().isSameType(a.getAnnotationType(), this.provideAnnotationType)).findFirst();
		if(provideAnnotation.isPresent()) {
			for(Entry<? extends ExecutableElement, ? extends AnnotationValue> value : this.processingEnvironment.getElementUtils().getElementValuesWithDefaults(provideAnnotation.get()).entrySet()) {
				switch(value.getKey().getSimpleName().toString()) {
					case "value" : providedType = (TypeMirror)value.getValue().getValue();
						break;
				}
			}
		}
		List<? extends TypeMirror> provideAnnotatedSuperTypes = this.processingEnvironment.getTypeUtils().directSupertypes(typeElement.asType()).stream()
			.filter(superType -> superType.getAnnotationMirrors().stream().anyMatch(a -> this.processingEnvironment.getTypeUtils().isSameType(a.getAnnotationType(), this.provideAnnotationType)))
			.collect(Collectors.toList());
		
		if(providedType != null && provideAnnotatedSuperTypes.size() == 1 || provideAnnotatedSuperTypes.size() > 1) {
			beanReporter.error("Bean " + beanQName + " can't provide multiple types");
		}
		else if(provideAnnotatedSuperTypes.size() == 1) {
			TypeMirror[] superTypeArguments = ((DeclaredType)provideAnnotatedSuperTypes.get(0)).getTypeArguments().stream().toArray(TypeMirror[]::new);
			TypeElement superTypeElement = (TypeElement)((DeclaredType)this.processingEnvironment.getTypeUtils().erasure(provideAnnotatedSuperTypes.get(0))).asElement();
			
			providedType = this.processingEnvironment.getTypeUtils().getDeclaredType(superTypeElement, superTypeArguments);
			provideAnnotation = provideAnnotatedSuperTypes.get(0).getAnnotationMirrors().stream().filter(a -> this.processingEnvironment.getTypeUtils().isSameType(a.getAnnotationType(), this.provideAnnotationType)).findFirst();
		}
		
		if(providedType != null && !this.processingEnvironment.getTypeUtils().isAssignable(beanType, providedType)) {
			this.processingEnvironment.getMessager().printMessage(Kind.ERROR, "Type " + providedType + " is incompatible with bean type " + beanType, typeElement, provideAnnotation.get());
			throw new BeanCompilationException();
		}
		
		return providedType;
	}
	
	private List<ExecutableElement> getInitMethods(TypeElement typeElement) {
		return typeElement.getEnclosedElements().stream()
			.filter(e -> e.getAnnotation(Init.class) != null)
			.map(e -> (ExecutableElement)e)
			.filter(e -> {
				if(!((ExecutableElement)e).getParameters().isEmpty()) {
					this.processingEnvironment.getMessager().printMessage(Kind.MANDATORY_WARNING, "Invalid " + Init.class.getSimpleName() + " method which should be a no-argument method, it will be ignored", e);
					return false;
				}
				return true;
			}).collect(Collectors.toList());
	}
	
	private List<ExecutableElement> getDestroyMethods(TypeElement typeElement) {
		return typeElement.getEnclosedElements().stream()
			.filter(e -> e.getAnnotation(Destroy.class) != null)
			.map(e -> (ExecutableElement)e)
			.filter(e -> {
				if(!e.getParameters().isEmpty()) {
					this.processingEnvironment.getMessager().printMessage(Kind.MANDATORY_WARNING, "Invalid " + Destroy.class.getSimpleName() + " method which should be a no-argument method, it will be ignored", e);
					return false;
				}
				return true;
			}).collect(Collectors.toList());
	}
	
	private List<ModuleBeanSocketInfo> getRequiredBeanSocketInfos(TypeElement typeElement, ReporterInfo beanReporter, ModuleBeanSocketInfoFactory beanSocketFactory, BeanQualifiedName beanQName) {
		ExecutableElement constructorSocketElement = null;
		
		List<ExecutableElement> constructorSocketElements = typeElement.getEnclosedElements().stream()
			.filter(e -> e.getKind().equals(ElementKind.CONSTRUCTOR))
			.filter(e -> e.getModifiers().stream().anyMatch(m -> m.equals(Modifier.PUBLIC)))
			.map(e -> (ExecutableElement)e)
			.collect(Collectors.toList());
		
		if(constructorSocketElements.stream().anyMatch(e -> e.getAnnotation(BeanSocket.class) != null)) {
			// only keep enabled socket
			constructorSocketElements = constructorSocketElements.stream()
				.filter(e -> e.getAnnotationMirrors().stream()
					.filter(a -> this.processingEnvironment.getTypeUtils().isSameType(a.getAnnotationType(), this.beanSocketAnnotationType))
					.findFirst()
					.map(beanSocketAnnotation -> {
						for(Entry<? extends ExecutableElement, ? extends AnnotationValue> value : this.processingEnvironment.getElementUtils().getElementValuesWithDefaults(beanSocketAnnotation).entrySet()) {
							switch(value.getKey().getSimpleName().toString()) {
								case "enabled" : return (boolean)value.getValue().getValue();
							}
						}
						return true;
					})
					.orElse(false)
				)
				.collect(Collectors.toList());
			
			if(constructorSocketElements.isEmpty()) {
				beanReporter.error("No constructor annotated with " + BeanSocket.class.getSimpleName() + " is enabled in module bean " + beanQName + ", consider enabling one constructor");
			}
			else if(constructorSocketElements.size() == 1) {
				constructorSocketElement = constructorSocketElements.get(0);
			}
			else {
				beanReporter.error("Multiple constructors annotated with " + BeanSocket.class.getSimpleName() + " are enabled in module bean " + beanQName + ", consider keeping only one enabled constructor");
			}
		}
		else {
			// Implicit sockets
			if(constructorSocketElements.isEmpty()) {
				// This should never happen
				beanReporter.error("No public constructor defined in bean " + beanQName);
			}
			else if(constructorSocketElements.size() == 1) {
				// OK
				constructorSocketElement = constructorSocketElements.get(0);
			}
			else {
				beanReporter.error("Multiple constructors are defined in module bean " + beanQName + ", consider specifying a " + BeanSocket.class.getSimpleName() + " on the one to select");
			}
		}
		
		if(constructorSocketElement != null) {
			return constructorSocketElement.getParameters().stream().map(ve -> {
				try {
					return beanSocketFactory.createBeanSocket(ve).orElse(null);
				} 
				catch (TypeErrorException e1) {
					beanReporter.error("Invalid required socket " + ve.getSimpleName().toString() + " : Type " + e1.getType() + " could not be resolved");
				}
				return null;
			})
			.filter(Objects::nonNull)
			.collect(Collectors.toList());
		}
		
		return List.of();
	}
	
	private List<ModuleBeanSocketInfo> getOptionalBeanSocketInfos(TypeElement typeElement, ModuleBeanSocketInfoFactory beanSocketFactory, List<ModuleBeanSocketInfo> requiredBeanSocketInfos) {
		List<ExecutableElement> optionalSocketElements = (List<ExecutableElement>)typeElement.getEnclosedElements().stream()
			.filter(e -> e.getKind().equals(ElementKind.METHOD) && e.getSimpleName().toString().startsWith("set"))
			.filter(e -> e.getModifiers().stream().anyMatch(m -> m.equals(Modifier.PUBLIC)))
			.map(e -> (ExecutableElement)e)
			.filter(e -> e.getParameters().size() == 1)
			.collect(Collectors.toList());
		
		if(optionalSocketElements.stream().anyMatch(e -> e.getAnnotation(BeanSocket.class) != null)) {
			// only keep enabled socket
			optionalSocketElements = optionalSocketElements.stream()
				.filter(e -> e.getAnnotationMirrors().stream()
					.filter(a -> this.processingEnvironment.getTypeUtils().isSameType(a.getAnnotationType(), this.beanSocketAnnotationType))
					.findFirst()
					.map(beanSocketAnnotation -> {
						for(Entry<? extends ExecutableElement, ? extends AnnotationValue> value : this.processingEnvironment.getElementUtils().getElementValuesWithDefaults(beanSocketAnnotation).entrySet()) {
							switch(value.getKey().getSimpleName().toString()) {
								case "enabled" : return (boolean)value.getValue().getValue();
							}
						}
						return true;
					})
					.orElse(false)
				)
				.collect(Collectors.toList());
		}

		final Map<String, ModuleBeanSocketInfo> requiredSocketByName = requiredBeanSocketInfos.stream().collect(Collectors.toMap(beanSocket -> beanSocket.getQualifiedName().getName(), Function.identity()));
		Predicate<ModuleBeanSocketInfo> requiredSocketConflictPredicate = optionalBeanSocketInfo -> {
			if(requiredSocketByName.containsKey(optionalBeanSocketInfo.getQualifiedName().getName())) {
				requiredSocketByName.get(optionalBeanSocketInfo.getQualifiedName().getName()).error("Required socket name is conflicting with an optional socket: " + optionalBeanSocketInfo.getQualifiedName().getName());
				optionalBeanSocketInfo.error("Optional socket name is conflicting with a required socket: " + optionalBeanSocketInfo.getQualifiedName().getName());
				return false;
			}
			return true;
		};
		
		List<ModuleBeanSocketInfo> optionalBeanSocketInfos = new ArrayList<>();
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
					.forEach(optionalBeanSocketInfos::add);
			}
			else {
				try {
					beanSocketFactory.createBeanSocket(socketElementsBySocketName.get(0).getParameters().get(0))
						.filter(requiredSocketConflictPredicate)
						.ifPresent(optionalBeanSocketInfos::add);
				}
				catch (TypeErrorException e1) {
					this.processingEnvironment.getMessager().printMessage(Kind.MANDATORY_WARNING, "Ignoring invalid optional socket: Type " + e1.getType() + " could not be resolved", socketElementsBySocketName.get(0));
				}
			}
		}
		return optionalBeanSocketInfos;
	}
}
