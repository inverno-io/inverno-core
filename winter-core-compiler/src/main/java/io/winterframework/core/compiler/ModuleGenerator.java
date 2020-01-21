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
package io.winterframework.core.compiler;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Element;
import javax.tools.Diagnostic.Kind;
import javax.tools.FileObject;
import javax.tools.JavaFileObject;
import javax.tools.StandardLocation;

import io.winterframework.core.compiler.ModuleClassGeneration.GenerationMode;
import io.winterframework.core.compiler.spi.ModuleBeanInfo;
import io.winterframework.core.compiler.spi.ModuleInfo;
import io.winterframework.core.compiler.spi.ModuleInfoBuilder;
import io.winterframework.core.compiler.spi.SocketBeanInfo;

/**
 * <p>
 * A Module generator is used to generate Winter module classes in the correct
 * order taking compilation round into account.
 * </p>
 * 
 * <p>
 * A module can't be generated if one or more of the modules it requires are not
 * compiled and generated yet.
 * </p>
 * 
 * @author jkuhn
 */
class ModuleGenerator {

	private ModuleAnnotationProcessor.Options options;
	
	private Map<String, ModuleInfo> generatedModules;
	
	private Map<String, ModuleInfo> requiredModules;
	
	private Set<String> faultyModules;
	
	private ProcessingEnvironment processingEnv;
	private ModuleClassGenerator moduleClassGenerator;
	private ModuleDescriptorGenerator moduleDescriptorGenerator;
	
	private Map<String, ModuleInfoBuilder> moduleBuilders;
	private Map<String, List<Element>> moduleOriginatingElements;
	private Map<String, List<ModuleBeanInfo>> moduleBeans;
	private Map<String, List<SocketBeanInfo>> moduleSockets;
	private Map<String, List<ModuleInfoBuilder>> requiredModuleBuilders;
	
	public ModuleGenerator(ProcessingEnvironment processingEnv, ModuleAnnotationProcessor.Options options) {
		this.processingEnv = processingEnv;
		this.options = options;
		this.moduleClassGenerator = new ModuleClassGenerator();
		this.moduleDescriptorGenerator = new ModuleDescriptorGenerator();

		this.generatedModules = new HashMap<>();
		this.requiredModules = new HashMap<>();
		this.faultyModules = new HashSet<>();
	}
	
	public ModuleGenerator forModules(Map<String, ModuleInfoBuilder> moduleBuilders) {
		this.moduleBuilders = moduleBuilders;
		return this;
	}
	
	public ModuleGenerator withOriginatingElements(Map<String, List<Element>> moduleOriginatingElements) {
		this.moduleOriginatingElements = moduleOriginatingElements;
		return this;
	}
	
	public ModuleGenerator withModuleBeans(Map<String, List<ModuleBeanInfo>> moduleBeans) {
		this.moduleBeans = moduleBeans;
		return this;
	}
	
	public ModuleGenerator withModuleSockets(Map<String, List<SocketBeanInfo>> moduleSockets) {
		this.moduleSockets = moduleSockets;
		return this;
	}
	
	public ModuleGenerator withRequiredModules(Map<String, List<ModuleInfoBuilder>> requiredModuleBuilders) {
		this.requiredModuleBuilders = requiredModuleBuilders;
		return this;
	}
	
	public boolean generateNextRound() {
		if(this.generatedModules.size() + this.faultyModules.size() == this.moduleBuilders.size()) {
			return false;
		}
		Map<String, ModuleInfo> roundModules = new HashMap<>();
		Map<String, ModuleInfo> roundGeneratedModules = new HashMap<>();
		Set<String> roundFaultyModules = new HashSet<>();
		for(ModuleInfoBuilder moduleBuilder : this.moduleBuilders.values()) {
			String moduleName = moduleBuilder.getQualifiedName().toString();
			if(!this.generatedModules.containsKey(moduleName) && !this.faultyModules.contains(moduleName) && !roundModules.containsKey(moduleBuilder.getQualifiedName().toString())) {
				this.generateModule(moduleBuilder, roundModules, roundGeneratedModules, roundFaultyModules);
			}
		}
		if(roundGeneratedModules.size() == 0 && roundFaultyModules.size() == 0) {
			throw new IllegalStateException("Module generator round resulted in 0 module generation. Unable to generate modules: " + this.moduleBuilders.keySet().stream().filter(moduleName -> !this.generatedModules.containsKey(moduleName)).collect(Collectors.joining(", ")));
		}
		this.generatedModules.putAll(roundGeneratedModules);
		this.faultyModules.addAll(roundFaultyModules);
		return true;
	}
	
	private ModuleInfo generateModule(ModuleInfoBuilder moduleBuilder, Map<String, ModuleInfo> roundModules, Map<String, ModuleInfo> roundGeneratedModules, Set<String> roundFaultyModules) {
		String moduleName = moduleBuilder.getQualifiedName().toString();
	
		boolean generate = true;
		if(this.moduleBeans.containsKey(moduleName)) {
			moduleBuilder.beans(this.moduleBeans.get(moduleName).stream().toArray(ModuleBeanInfo[]::new));
		}
		if(this.moduleSockets.containsKey(moduleName)) {
			moduleBuilder.sockets(this.moduleSockets.get(moduleName).stream().toArray(SocketBeanInfo[]::new));					
		}
		if(this.requiredModuleBuilders.containsKey(moduleName)) {
			List<ModuleInfo> requiredModules = new ArrayList<>();
			for(ModuleInfoBuilder requiredModuleBuilder : this.requiredModuleBuilders.get(moduleName)) {
				String requiredModuleName = requiredModuleBuilder.getQualifiedName().toString();
				if(this.generatedModules.containsKey(requiredModuleName)) {
					// Previous rounds
					requiredModules.add(this.generatedModules.get(requiredModuleName));
				}
				else if(this.requiredModules.containsKey(requiredModuleName)) {
					// compiled module
					requiredModules.add(this.requiredModules.get(requiredModuleName));
				} 
				else if(this.faultyModules.contains(requiredModuleName)) {
					// Faulty module
					roundFaultyModules.add(moduleName);
				}
				else if(this.moduleBuilders.containsValue(requiredModuleBuilder)) {
					// Compiling Module
					generate = false;
					if(roundModules.containsKey(requiredModuleName)) {
						requiredModules.add(roundModules.get(requiredModuleName));
					}
					else {
						ModuleInfo requiredModule = this.generateModule(requiredModuleBuilder, roundModules, roundGeneratedModules, roundFaultyModules);
						requiredModules.add(requiredModule);
					}
				}
				else {
					// Required Module
					ModuleInfo requiredModule = requiredModuleBuilder.build();
					this.requiredModules.put(requiredModuleName, requiredModule);
					requiredModules.add(requiredModule);
				}
			}
			moduleBuilder.modules(requiredModules.toArray(new ModuleInfo[requiredModules.size()]));
		}
		
		ModuleInfo moduleInfo = moduleBuilder.build();
		roundModules.put(moduleName, moduleInfo);
		if(moduleInfo.isFaulty()) {
			roundFaultyModules.add(moduleName);
		}

		if(generate && !moduleInfo.isFaulty()) {
			// Descriptor
			if(this.options.isVerbose()) {
				System.out.println(moduleInfo.accept(this.moduleDescriptorGenerator, ""));
			}
			
			if(this.options.isGenerateModuleDescriptor()) {
				try {
					FileObject moduleDescriptorFile = this.processingEnv.getFiler().createResource(StandardLocation.CLASS_OUTPUT, moduleInfo.getQualifiedName().getValue() + "/", "META-INF/winter/module.yml", this.moduleOriginatingElements.get(moduleName).stream().toArray(Element[]::new));
					try (Writer writer = moduleDescriptorFile.openWriter()) {
						writer.write(moduleInfo.accept(this.moduleDescriptorGenerator, ""));
						writer.flush();
					}
				} 
				catch (IOException e) {
					this.processingEnv.getMessager().printMessage(Kind.MANDATORY_WARNING, "Error generating Module descriptor " + moduleInfo.getQualifiedName() + ": " + e.getMessage());
					if(this.options.isDebug()) {
						e.printStackTrace();
					}
				}
			}
			
			try {
				JavaFileObject moduleSourceFile = this.processingEnv.getFiler().createSourceFile(moduleInfo.getQualifiedName().getClassName(), this.moduleOriginatingElements.get(moduleName).stream().toArray(Element[]::new));
				try (Writer writer = moduleSourceFile.openWriter()) {
					writer.write(moduleInfo.accept(this.moduleClassGenerator, new ModuleClassGeneration(this.processingEnv, GenerationMode.MODULE_CLASS)));
					writer.flush();
				}
				
				if(this.options.isVerbose()) {
					System.out.println("Module " + moduleInfo.getQualifiedName() + " generated to " + moduleSourceFile.toUri());
				}
//				this.processingEnv.getMessager().printMessage(Kind.NOTE, "Module " + moduleInfo.getQualifiedName() + " generated to " + moduleSourceFile.toUri());
			} 
			catch (IOException e) {
				this.processingEnv.getMessager().printMessage(Kind.ERROR, "Error generating Module " + moduleInfo.getQualifiedName() + ": " + e.getMessage());
				if(this.options.isDebug()) {
					e.printStackTrace();
				}
			}
			roundGeneratedModules.put(moduleName, moduleInfo);
		}
		return moduleInfo;
	}
}
