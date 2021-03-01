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
package io.winterframework.core.compiler;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedOptions;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.ModuleElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import javax.tools.Diagnostic.Kind;

import io.winterframework.core.annotation.Bean;
import io.winterframework.core.annotation.Module;
import io.winterframework.core.annotation.NestedBean;
import io.winterframework.core.compiler.bean.BeanCompilationException;
import io.winterframework.core.compiler.bean.ModuleBeanInfoFactory;
import io.winterframework.core.compiler.module.ModuleInfoBuilderFactory;
import io.winterframework.core.compiler.module.ModuleMetadataExtractor;
import io.winterframework.core.compiler.socket.SocketBeanInfoFactory;
import io.winterframework.core.compiler.socket.SocketCompilationException;
import io.winterframework.core.compiler.spi.ModuleBeanInfo;
import io.winterframework.core.compiler.spi.ModuleInfoBuilder;
import io.winterframework.core.compiler.spi.SocketBeanInfo;

/**
 * <p>
 * Winter compiler annotation processor which processes {@link Module} and
 * {@link Bean} annotations and generate a Winter module class.
 * </p>
 * 
 * @author jkuhn
 *
 */
//@SupportedAnnotationTypes({"io.winterframework.core.annotation/io.winterframework.core.annotation.Module","io.winterframework.core.annotation/io.winterframework.core.annotation.Bean"})
@SupportedAnnotationTypes({"io.winterframework.core.annotation.Module","io.winterframework.core.annotation.Bean"})
@SupportedOptions({GenericCompilerOptions.DEBUG, GenericCompilerOptions.VERBOSE, GenericCompilerOptions.GENERATE_DESCRIPTOR})
public class WinterCompiler extends AbstractProcessor {

	public static final int VERSION = 1;
	
	private ModuleGenerator moduleGenerator;
	
	private Map<String, SocketBeanInfoFactory> socketFactories = new TreeMap<>(Collections.reverseOrder());
	private Map<String, ModuleBeanInfoFactory> beanFactories = new TreeMap<>(Collections.reverseOrder());
	
	@Override
	public synchronized void init(ProcessingEnvironment processingEnv) {
		super.init(processingEnv);
		this.moduleGenerator = new ModuleGenerator(processingEnv, new GenericCompilerOptions(processingEnv.getOptions()));
		
		this.socketFactories = new TreeMap<>(Collections.reverseOrder());
		this.beanFactories = new TreeMap<>(Collections.reverseOrder());
	}
	
	@Override
	public SourceVersion getSupportedSourceVersion() {
		return this.processingEnv.getSourceVersion().compareTo(SourceVersion.RELEASE_9) < 0 ? SourceVersion.RELEASE_9 : this.processingEnv.getSourceVersion();
	}
	
	@Override
	public Set<String> getSupportedAnnotationTypes() {
		return Stream.concat(super.getSupportedAnnotationTypes().stream(), this.moduleGenerator.getPluginsExecutor().getPlugins().stream().filter(plugin -> plugin.getSupportedAnnotationTypes() != null).flatMap(plugin -> plugin.getSupportedAnnotationTypes().stream())).collect(Collectors.toSet());
	}
	
	@Override
	public Set<String> getSupportedOptions() {
		return Stream.concat(super.getSupportedOptions().stream(), this.moduleGenerator.getPluginsExecutor().getPlugins().stream().filter(plugin -> plugin.getSupportedOptions() != null).flatMap(plugin -> plugin.getSupportedOptions().stream())).collect(Collectors.toSet());
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
		this.processingEnv.getElementUtils().getTypeElement(NestedBean.class.getCanonicalName()).asType();
		
		TypeMirror moduleAnnotationType = this.processingEnv.getElementUtils().getTypeElement(Module.class.getCanonicalName()).asType();
		TypeMirror beanAnnotationType = this.processingEnv.getElementUtils().getTypeElement(Bean.class.getCanonicalName()).asType();
		
		Map<String, Set<Element>> moduleOriginatingElements = new HashMap<>();
		
		this.moduleGenerator
			.putModules(roundEnv.getElementsAnnotatedWith(Module.class).stream()
				.map(element -> (ModuleElement)element)
				.map(moduleElement -> {
					String moduleName = moduleElement.getQualifiedName().toString();
					
					ModuleInfoBuilder moduleInfoBuilder = ModuleInfoBuilderFactory.createModuleBuilder(this.processingEnv, moduleElement);
					moduleOriginatingElements.put(moduleName, new HashSet<>(List.of(moduleElement)));
					
					this.beanFactories.put(moduleName, ModuleBeanInfoFactory.create(this.processingEnv, moduleElement));
					this.socketFactories.put(moduleName, SocketBeanInfoFactory.create(this.processingEnv, moduleElement));
					
					return moduleInfoBuilder;
				})
				.collect(Collectors.toMap(moduleInfoBuilder -> moduleInfoBuilder.getQualifiedName().toString(), Function.identity()))
			)
			.putModuleBeans(roundEnv.getElementsAnnotatedWith(Bean.class).stream()
				.filter(element -> element.getKind().equals(ElementKind.CLASS))
				.map(element -> {
					AnnotationMirror beanAnnotation = element.getAnnotationMirrors().stream().filter(a -> this.processingEnv.getTypeUtils().isSameType(a.getAnnotationType(), beanAnnotationType)).findFirst().get();
					ModuleElement moduleElement = this.processingEnv.getElementUtils().getModuleOf(element);
					if(moduleElement == null) {
						return null;
					}
					String moduleName = moduleElement.getQualifiedName().toString();
					if(!this.beanFactories.containsKey(moduleName)) {
						this.processingEnv.getMessager().printMessage(Kind.MANDATORY_WARNING, "Bean might be out of sync with the module please consider recompiling the module" , element, beanAnnotation);
						return null;
					}
					ModuleBeanInfoFactory beanFactory = this.beanFactories.get(moduleName);
					ModuleBeanInfo moduleBean;
					try {
						moduleBean = beanFactory.createBean(element);
					}
					catch (BeanCompilationException e) {
						return null;
					}
					catch (Exception e) {
						this.processingEnv.getMessager().printMessage(Kind.WARNING, "Unable to create bean: " + e.getMessage(), element, beanAnnotation);
						return null;
					}
					
					if(!moduleOriginatingElements.containsKey(moduleName)) {
						moduleOriginatingElements.put(moduleName, new HashSet<>());
					}
					moduleOriginatingElements.get(moduleName).add(element);
					return moduleBean;
				})
				.filter(Objects::nonNull)
				.collect(Collectors.groupingBy(moduleBean -> moduleBean.getQualifiedName().getModuleQName().getValue()))
			)
			.putModuleSockets(roundEnv.getElementsAnnotatedWith(Bean.class).stream()
				.filter(element -> element.getKind().equals(ElementKind.INTERFACE))
				.map(element -> {
					AnnotationMirror beanAnnotation = element.getAnnotationMirrors().stream().filter(a -> this.processingEnv.getTypeUtils().isSameType(a.getAnnotationType(), beanAnnotationType)).findFirst().get();
					ModuleElement moduleElement = this.processingEnv.getElementUtils().getModuleOf(element);
					if(moduleElement == null) {
						return null;
					}
					String moduleName = moduleElement.getQualifiedName().toString();
					if(!this.socketFactories.containsKey(moduleName)) {
						this.processingEnv.getMessager().printMessage(Kind.MANDATORY_WARNING, "Socket bean might be out of sync with the module please consider recompiling the module", element, beanAnnotation);
						return null;
					}
					SocketBeanInfoFactory socketFactory = this.socketFactories.get(moduleName);
					SocketBeanInfo moduleSocket;
					try {
						moduleSocket = socketFactory.createSocketBean(element);
					}
					catch (SocketCompilationException e) {
						return null;
					}
					catch (Exception e) {
						this.processingEnv.getMessager().printMessage(Kind.WARNING, "Unable to create socket bean: " + e.getMessage(), element, beanAnnotation);
						return null;
					}

					if(!moduleOriginatingElements.containsKey(moduleName)) {
						moduleOriginatingElements.put(moduleName, new HashSet<>());
					}
					moduleOriginatingElements.get(moduleName).add(element);
					return moduleSocket;
				})
				.filter(Objects::nonNull)
				.collect(Collectors.groupingBy(socket -> socket.getQualifiedName().getModuleQName().getValue()))
			)
			.putComponentModules(roundEnv.getElementsAnnotatedWith(Module.class).stream()
				.collect(Collectors.toMap(
					element -> ((ModuleElement)element).getQualifiedName().toString(), 
					element -> {
						ModuleElement moduleElement = (ModuleElement)element;

						AnnotationMirror moduleAnnotation = this.processingEnv.getElementUtils().getAllAnnotationMirrors(moduleElement).stream().filter(a -> this.processingEnv.getTypeUtils().isSameType(a.getAnnotationType(), moduleAnnotationType)).findFirst().get();
						
						final Set<String> includes = new HashSet<>();
						final Set<String> excludes = new HashSet<>();
						for(Entry<? extends ExecutableElement, ? extends AnnotationValue> value : this.processingEnv.getElementUtils().getElementValuesWithDefaults(moduleAnnotation).entrySet()) {
							switch(value.getKey().getSimpleName().toString()) {
								case "includes" : includes.addAll(((List<AnnotationValue>)value.getValue().getValue()).stream().map(v -> v.getValue().toString()).collect(Collectors.toSet()));
									break;
								case "excludes" : excludes.addAll(((List<AnnotationValue>)value.getValue().getValue()).stream().map(v -> v.getValue().toString()).collect(Collectors.toSet()));
									break;
							}
						}
						
						return moduleElement.getDirectives().stream()
							.filter(directive -> {
								if(!directive.getKind().equals(ModuleElement.DirectiveKind.REQUIRES)) {
									return false;
								}
								
								ModuleElement componentModuleElement = ((ModuleElement.RequiresDirective)directive).getDependency();

								if( (excludes.size() > 0 && excludes.contains(componentModuleElement.getQualifiedName().toString())) || (includes.size() > 0 && !includes.contains(componentModuleElement.getQualifiedName().toString()))) {
									return false;
								}
								
								Optional<? extends AnnotationMirror> componentModuleAnnotation = this.processingEnv.getElementUtils().getAllAnnotationMirrors(componentModuleElement).stream().filter(a -> this.processingEnv.getTypeUtils().isSameType(a.getAnnotationType(), moduleAnnotationType)).findFirst();
								return componentModuleAnnotation.isPresent();
							})
							.map(directive -> {
								ModuleElement componentModuleElement = ((ModuleElement.RequiresDirective)directive).getDependency();
								String componentModuleName = componentModuleElement.getQualifiedName().toString();
								if(this.moduleGenerator.modules().containsKey(componentModuleName)) {
									return this.moduleGenerator.modules().get(componentModuleName);
								}
							
								return this.processComponentModule(moduleElement, componentModuleElement);
							})
							.collect(Collectors.toList());
					}))
			)
			.putOriginatingElements(moduleOriginatingElements);
			
		this.moduleGenerator.generateNextRound(roundEnv);
		return true;
	}
	
	private ModuleInfoBuilder processComponentModule(ModuleElement moduleElement, ModuleElement componentModuleElement) {
		ModuleMetadataExtractor moduleMetadataExtractor = new ModuleMetadataExtractor(this.processingEnv, componentModuleElement);
		if(moduleMetadataExtractor.getModuleVersion() == null) {
			throw new IllegalStateException("Version of component module " + moduleMetadataExtractor.getModuleQualifiedName().toString() + " can't be null");			
		}
		TypeElement moduleType = this.processingEnv.getElementUtils().getTypeElement(moduleMetadataExtractor.getModuleQualifiedName().getClassName());
		
		switch(moduleMetadataExtractor.getModuleVersion()) {
			case 1: return this.processComponentModuleV1(moduleElement, componentModuleElement, moduleType);
			default: throw new IllegalStateException("Version of module " + moduleMetadataExtractor.getModuleQualifiedName().toString() + " is not supported: " + moduleMetadataExtractor.getModuleVersion());
		}
	}
	
	private ModuleInfoBuilder processComponentModuleV1(ModuleElement moduleElement, ModuleElement componentModuleElement, TypeElement moduleType) {
		ModuleInfoBuilder componentModuleBuilder = ModuleInfoBuilderFactory.createModuleBuilder(this.processingEnv, moduleElement, componentModuleElement, 1);
		
		SocketBeanInfoFactory componentModuleSocketFactory = SocketBeanInfoFactory.create(this.processingEnv, moduleElement, componentModuleElement, 1);
	
		List<? extends SocketBeanInfo> componentModuleSockets = ((ExecutableElement)moduleType.getEnclosedElements().stream()
			.filter(e -> e.getKind().equals(ElementKind.CONSTRUCTOR)).findFirst().get())
			.getParameters().stream()
			.map(ve -> {
				try {
					return componentModuleSocketFactory.createSocketBean(ve);
				} 
				catch (SocketCompilationException e1) {
					return null;
				}
			})
			.collect(Collectors.toList());

		componentModuleBuilder.sockets(componentModuleSockets.stream().toArray(SocketBeanInfo[]::new));

		ModuleBeanInfoFactory componentModuleBeanFactory = ModuleBeanInfoFactory.create(this.processingEnv, moduleElement, componentModuleElement, () -> componentModuleSockets, 1);
		List<? extends ModuleBeanInfo> componentModuleBeans = moduleType.getEnclosedElements().stream()
			.filter(e -> e.getKind().equals(ElementKind.METHOD) && e.getModifiers().contains(Modifier.PUBLIC) && !e.getModifiers().contains(Modifier.STATIC) && ((ExecutableElement)e).getParameters().size() == 0)
			.map(e -> {
				try {
					return componentModuleBeanFactory.createBean(e);
				} 
				catch (BeanCompilationException e1) {
					return null;
				}
			})
			.filter(Objects::nonNull)
			.collect(Collectors.toList());

		componentModuleBuilder.beans(componentModuleBeans.stream().toArray(ModuleBeanInfo[]::new));
	
		return componentModuleBuilder;
	}
}
