/*
 * Copyright 2019 Jeremy KUHN
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
package io.inverno.core.compiler;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.annotation.processing.FilerException;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.Element;
import javax.tools.Diagnostic.Kind;
import javax.tools.FileObject;
import javax.tools.JavaFileObject;
import javax.tools.StandardLocation;

import io.inverno.core.compiler.ModuleClassGenerationContext.GenerationMode;
import io.inverno.core.compiler.plugin.PluginsExecutionResult;
import io.inverno.core.compiler.plugin.PluginsExecutionTask;
import io.inverno.core.compiler.plugin.PluginsExecutor;
import io.inverno.core.compiler.spi.BeanInfo;
import io.inverno.core.compiler.spi.ModuleBeanInfo;
import io.inverno.core.compiler.spi.ModuleInfo;
import io.inverno.core.compiler.spi.ModuleInfoBuilder;
import io.inverno.core.compiler.spi.SocketBeanInfo;

/**
 * <p>
 * A Module generator is used to generate Inverno module classes in the correct order taking compilation round into account.
 * </p>
 *
 * <p>
 * A module can't be generated if one or more of the modules it requires are not compiled and generated yet.
 * </p>
 * 
 * @author <a href="mailto:jeremy.kuhn@inverno.io">Jeremy Kuhn</a>
 */
class ModuleGenerator {

	private final GenericCompilerOptions options;
	
	private final Map<String, ModuleInfo> generatedModules;
	private final Map<String, ModuleInfo> componentModules;
	private final Set<String> faultyModules;
	private final Map<String, PluginsExecutionResult> pluginsExecutedModules;
	
	private final ProcessingEnvironment processingEnvironment;
	
	private final ModuleClassGenerator moduleClassGenerator;
	private final ModuleDescriptorGenerator moduleDescriptorGenerator;
	
	private final Map<String, ModuleInfoBuilder> moduleBuilders;
	private final Map<String, Set<Element>> moduleOriginatingElements;
	private final Map<String, List<ModuleBeanInfo>> moduleBeans;
	private final Map<String, List<SocketBeanInfo>> moduleSockets;
	private final Map<String, List<ModuleInfoBuilder>> componentModuleBuilders;
	
	private final PluginsExecutor pluginsExecutor;
	
	public ModuleGenerator(ProcessingEnvironment processingEnv, GenericCompilerOptions options) {
		this.processingEnvironment = processingEnv;
		this.options = options;
		this.moduleClassGenerator = new ModuleClassGenerator();
		this.moduleDescriptorGenerator = new ModuleDescriptorGenerator();

		this.generatedModules = new HashMap<>();
		this.componentModules = new HashMap<>();
		this.faultyModules = new HashSet<>();
		this.pluginsExecutedModules = new HashMap<>();
		
		this.moduleBuilders = new HashMap<>();
		this.moduleOriginatingElements = new HashMap<>();
		this.moduleBeans = new HashMap<>();
		this.moduleSockets = new HashMap<>();
		this.componentModuleBuilders = new HashMap<>();
		
		this.pluginsExecutor = new PluginsExecutor(this.processingEnvironment, this.options);
	}
	
	public PluginsExecutor getPluginsExecutor() {
		return pluginsExecutor;
	}
	
	public ModuleGenerator putModules(Map<String, ModuleInfoBuilder> moduleBuilders) {
		this.moduleBuilders.putAll(moduleBuilders);
		return this;
	}
	
	public Map<String, ModuleInfoBuilder> modules() {
		return Collections.unmodifiableMap(this.moduleBuilders);
	}
	
	public ModuleGenerator putOriginatingElements(Map<String, Set<Element>> moduleOriginatingElements) {
		moduleOriginatingElements.entrySet().stream().forEach(e -> {
			if(this.moduleOriginatingElements.containsKey(e.getKey())) {
				this.moduleOriginatingElements.put(e.getKey(), Stream.concat(this.moduleOriginatingElements.get(e.getKey()).stream(), e.getValue().stream()).collect(Collectors.toSet()));
			}
			else {
				this.moduleOriginatingElements.put(e.getKey(), Collections.unmodifiableSet(e.getValue()));
			}
		});
		return this;
	}
	
	public Map<String, Set<Element>> originatingElements() {
		return Collections.unmodifiableMap(this.moduleOriginatingElements);
	}
	
	public ModuleGenerator putModuleBeans(Map<String, List<ModuleBeanInfo>> moduleBeans) {
		moduleBeans.entrySet().stream().forEach(e -> {
			if(this.moduleBeans.containsKey(e.getKey())) {
				this.moduleBeans.put(e.getKey(), Stream.concat(this.moduleBeans.get(e.getKey()).stream(), e.getValue().stream()).collect(Collectors.toList()));
			}
			else {
				this.moduleBeans.put(e.getKey(), Collections.unmodifiableList(e.getValue()));
			}
		});
		return this;
	}
	
	public Map<String, List<ModuleBeanInfo>> moduleBeans() {
		return Collections.unmodifiableMap(this.moduleBeans);
	}
	
	public ModuleGenerator putModuleSockets(Map<String, List<SocketBeanInfo>> moduleSockets) {
		moduleSockets.entrySet().stream().forEach(e -> {
			if(this.moduleSockets.containsKey(e.getKey())) {
				this.moduleSockets.put(e.getKey(), Stream.concat(this.moduleSockets.get(e.getKey()).stream(), e.getValue().stream()).collect(Collectors.toList()));
			}
			else {
				this.moduleSockets.put(e.getKey(), Collections.unmodifiableList(e.getValue()));
			}
		});
		return this;
	}
	
	public Map<String, List<SocketBeanInfo>> moduleSockets() {
		return Collections.unmodifiableMap(this.moduleSockets);
	}
	
	public ModuleGenerator putComponentModules(Map<String, List<ModuleInfoBuilder>> componentModuleBuilders) {
		componentModuleBuilders.entrySet().stream().forEach(e -> {
			if(this.componentModuleBuilders.containsKey(e.getKey())) {
				this.componentModuleBuilders.put(e.getKey(), Stream.concat(this.componentModuleBuilders.get(e.getKey()).stream(), e.getValue().stream()).collect(Collectors.toList()));
			}
			else {
				this.componentModuleBuilders.put(e.getKey(), Collections.unmodifiableList(e.getValue()));
			}
		});
		return this;
	}
	
	public Map<String, List<ModuleInfoBuilder>> componentModules() {
		return Collections.unmodifiableMap(this.componentModuleBuilders);
	}
	
	public GenericCompilerOptions getOptions() {
		return options;
	}
	
	public boolean generateNextRound(RoundEnvironment roundEnv) {
		if(this.generatedModules.size() + this.faultyModules.size() == this.moduleBuilders.size()) {
			return false;
		}
		Map<String, ModuleInfo> roundModules = new HashMap<>();
		Map<String, ModuleInfo> roundGeneratedModules = new HashMap<>();
		Set<String> roundFaultyModules = new HashSet<>();
		Map<String, PluginsExecutionResult> roundPluginExecutedModules = new HashMap<>();
		for(ModuleInfoBuilder moduleBuilder : this.moduleBuilders.values()) {
			String moduleName = moduleBuilder.getQualifiedName().toString();
			if(!this.generatedModules.containsKey(moduleName) && !this.faultyModules.contains(moduleName) && !roundModules.containsKey(moduleBuilder.getQualifiedName().toString())) {
				this.generateModule(moduleBuilder, roundEnv, roundModules, roundGeneratedModules, roundFaultyModules, roundPluginExecutedModules);
			}
		}
		if(roundGeneratedModules.isEmpty() && roundFaultyModules.isEmpty() && roundPluginExecutedModules.isEmpty()) {
			throw new IllegalStateException("Module generator round resulted in no module generation and no plugin execution. Unable to generate modules: " + this.moduleBuilders.keySet().stream().filter(moduleName -> !this.generatedModules.containsKey(moduleName)).collect(Collectors.joining(", ")));
		}
		this.generatedModules.putAll(roundGeneratedModules);
		this.faultyModules.addAll(roundFaultyModules);
		this.pluginsExecutedModules.putAll(roundPluginExecutedModules);
		return true;
	}
	
	private ModuleInfo generateModule(ModuleInfoBuilder moduleBuilder, RoundEnvironment roundEnv, Map<String, ModuleInfo> roundModules, Map<String, ModuleInfo> roundGeneratedModules, Set<String> roundFaultyModules, Map<String, PluginsExecutionResult> roundPluginExecutedModules) {
		String moduleName = moduleBuilder.getQualifiedName().toString();
		
		List<BeanInfo> moduleInjectableBeans = new ArrayList<>();
		boolean generate = true;
		if(this.moduleBeans.containsKey(moduleName)) {
			moduleInjectableBeans.addAll(this.moduleBeans.get(moduleName));
			moduleBuilder.beans(this.moduleBeans.get(moduleName).stream().toArray(ModuleBeanInfo[]::new));
		}
		if(this.moduleSockets.containsKey(moduleName)) {
			moduleInjectableBeans.addAll(this.moduleSockets.get(moduleName));
			moduleBuilder.sockets(this.moduleSockets.get(moduleName).stream().toArray(SocketBeanInfo[]::new));					
		}
		List<ModuleInfo> currentComponentModules = new ArrayList<>();
		if(this.componentModuleBuilders.containsKey(moduleName)) {
			for(ModuleInfoBuilder componentModuleBuilder : this.componentModuleBuilders.get(moduleName)) {
				String componentModuleName = componentModuleBuilder.getQualifiedName().toString();
				if(this.generatedModules.containsKey(componentModuleName)) {
					// Previous rounds
					ModuleInfo componentModule = this.generatedModules.get(componentModuleName);
					Arrays.stream(componentModule.getPublicBeans()).forEach(moduleInjectableBeans::add);
					if(!componentModule.isEmpty()) {
						currentComponentModules.add(componentModule);
					}
				}
				else if(this.componentModules.containsKey(componentModuleName)) {
					// compiled module
					ModuleInfo componentModule = this.componentModules.get(componentModuleName);
					Arrays.stream(componentModule.getPublicBeans()).forEach(moduleInjectableBeans::add);
					if(!componentModule.isEmpty()) {
						currentComponentModules.add(componentModule);
					}
				} 
				else if(this.faultyModules.contains(componentModuleName)) {
					// Faulty module
					roundFaultyModules.add(moduleName);
				}
				else if(this.moduleBuilders.containsValue(componentModuleBuilder)) {
					// Compiling Module
					generate = false;
					ModuleInfo componentModule = null;
					if(roundModules.containsKey(componentModuleName)) {
						componentModule = roundModules.get(componentModuleName);
					}
					else {
						componentModule = this.generateModule(componentModuleBuilder, roundEnv, roundModules, roundGeneratedModules, roundFaultyModules, roundPluginExecutedModules);
					}
					
					if(componentModule != null) {
						Arrays.stream(componentModule.getPublicBeans()).forEach(moduleInjectableBeans::add);
						if(!componentModule.isEmpty()) {
							currentComponentModules.add(componentModule);
						}
					}
				}
				else {
					// Component Module
					ModuleInfo componentModule = componentModuleBuilder.build();
					this.componentModules.put(componentModuleName, componentModule);
					Arrays.stream(componentModule.getPublicBeans()).forEach(moduleInjectableBeans::add);
					if(!componentModule.isEmpty()) {
						currentComponentModules.add(componentModule);
					}
				}
			}
			if(generate) {
				moduleBuilder.modules(currentComponentModules.toArray(ModuleInfo[]::new));
			}
		}
		
		PluginsExecutionResult pluginsExecutionResult = this.getPreviousPluginsExecution(moduleBuilder, roundPluginExecutedModules);
		if(pluginsExecutionResult == null) {
			PluginsExecutionTask pluginExecutionTask = this.pluginsExecutor.getTask(moduleBuilder.getElement(), moduleBuilder.getQualifiedName(), moduleInjectableBeans, currentComponentModules);
			pluginExecutionTask.addRound(roundEnv);
			if(generate) {
				pluginsExecutionResult = pluginExecutionTask.call();
				roundPluginExecutedModules.put(moduleName, pluginsExecutionResult);
				generate = !pluginsExecutionResult.hasGeneratedSourceFiles();
			}
		}
		
		ModuleInfo moduleInfo = null;
		if(generate) {
			if(this.options.isVerbose()) {
				System.out.println("Generating module " + moduleBuilder.getQualifiedName().toString() + "...");
			}
			moduleInfo = moduleBuilder.build();
			if(moduleInfo.isFaulty() || pluginsExecutionResult.hasError()) {
				roundFaultyModules.add(moduleName);
			}
			else {
				// Descriptor
				if(this.options.isVerbose()) {
					System.out.println(moduleInfo.accept(this.moduleDescriptorGenerator, ""));
				}
				
				if(this.options.isGenerateModuleDescriptor()) {
					try {
						FileObject moduleDescriptorFile;
						try {
							// module oriented
							moduleDescriptorFile = this.processingEnvironment.getFiler().createResource(StandardLocation.CLASS_OUTPUT, moduleInfo.getQualifiedName().getValue() + "/", "META-INF/inverno/core/" + moduleInfo.getQualifiedName().getValue() + "/module.yml", this.moduleOriginatingElements.get(moduleName).stream().toArray(Element[]::new));
						}
						catch (FilerException e) {
							// not module oriented after all
							moduleDescriptorFile = this.processingEnvironment.getFiler().createResource(StandardLocation.CLASS_OUTPUT, "", "META-INF/inverno/core/" + moduleInfo.getQualifiedName().getValue() + "module.yml", this.moduleOriginatingElements.get(moduleName).stream().toArray(Element[]::new));
						}
						try (Writer writer = moduleDescriptorFile.openWriter()) {
							writer.write(moduleInfo.accept(this.moduleDescriptorGenerator, ""));
							writer.flush();
						}
					} 
					catch (IOException e) {
						this.processingEnvironment.getMessager().printMessage(Kind.MANDATORY_WARNING, "Error generating Module descriptor " + moduleInfo.getQualifiedName() + ": " + e.getMessage());
						if(this.options.isDebug()) {
							e.printStackTrace();
						}
					}
				}
				
				if(moduleInfo.getBeans().length > 0 || moduleInfo.getModules().length > 0) {
					// only generate module class when it defines beans or modules
					try {
						JavaFileObject moduleSourceFile = this.processingEnvironment.getFiler().createSourceFile(moduleInfo.getQualifiedName().getClassName(), this.moduleOriginatingElements.get(moduleName).stream().toArray(Element[]::new));
						try (Writer writer = moduleSourceFile.openWriter()) {
							writer.write(moduleInfo.accept(this.moduleClassGenerator, new ModuleClassGenerationContext(this.processingEnvironment.getTypeUtils(), this.processingEnvironment.getElementUtils(), GenerationMode.MODULE_CLASS)).toString());
							writer.flush();
						}

						if(this.options.isVerbose()) {
							System.out.println("Module " + moduleInfo.getQualifiedName() + " generated to " + moduleSourceFile.toUri() + "\n");
						}
						//this.processingEnv.getMessager().printMessage(Kind.NOTE, "Module " + moduleInfo.getQualifiedName() + " generated to " + moduleSourceFile.toUri());
					} 
					catch (IOException e) {
						this.processingEnvironment.getMessager().printMessage(Kind.ERROR, "Error generating Module " + moduleInfo.getQualifiedName() + ": " + e.getMessage());
						if(this.options.isDebug()) {
							e.printStackTrace();
						}
					}
				}
				roundGeneratedModules.put(moduleName, moduleInfo);
			}
		}
		roundModules.put(moduleName, moduleInfo);
		return moduleInfo;
	}
	
	private PluginsExecutionResult getPreviousPluginsExecution(ModuleInfoBuilder moduleBuilder, Map<String, PluginsExecutionResult> roundPluginExecutedModules) {
		PluginsExecutionResult executionResult = this.pluginsExecutedModules.get(moduleBuilder.getQualifiedName().toString());
		if(executionResult == null) {
			executionResult = roundPluginExecutedModules.get(moduleBuilder.getQualifiedName().toString());
		}
		return executionResult;
	}
}
