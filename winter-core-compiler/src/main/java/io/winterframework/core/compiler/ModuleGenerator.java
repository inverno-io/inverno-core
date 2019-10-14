/**
 * 
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
import javax.tools.JavaFileObject;

import io.winterframework.core.compiler.ModuleClassGeneration.GenerationMode;
import io.winterframework.core.compiler.spi.ModuleBeanInfo;
import io.winterframework.core.compiler.spi.ModuleInfo;
import io.winterframework.core.compiler.spi.ModuleInfoBuilder;
import io.winterframework.core.compiler.spi.SocketBeanInfo;

/**
 * @author jkuhn
 *
 */
class ModuleGenerator {

	private Map<String, ModuleInfo> generatedModules;
	
	private Map<String, ModuleInfo> importedModules;
	
	private Set<String> faultyModules;
	
	private ProcessingEnvironment processingEnv;
	private ModuleClassGenerator moduleClassGenerator;
	private ModuleReporter moduleReporter;
	
	private Map<String, ModuleInfoBuilder> moduleBuilders;
	private Map<String, List<Element>> moduleOriginatingElements;
	private Map<String, List<ModuleBeanInfo>> moduleBeans;
	private Map<String, List<SocketBeanInfo>> moduleSockets;
	private Map<String, List<ModuleInfoBuilder>> importedModuleBuilders;
	
	public ModuleGenerator(ProcessingEnvironment processingEnv) {
		this.processingEnv = processingEnv;
		this.moduleClassGenerator = new ModuleClassGenerator();
		this.moduleReporter = new ModuleReporter();

		this.generatedModules = new HashMap<>();
		this.importedModules = new HashMap<>();
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
	
	public ModuleGenerator withImportedModules(Map<String, List<ModuleInfoBuilder>> importedModuleBuilders) {
		this.importedModuleBuilders = importedModuleBuilders;
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
		if(this.importedModuleBuilders.containsKey(moduleName)) {
			List<ModuleInfo> importedModules = new ArrayList<>();
			for(ModuleInfoBuilder importedModuleBuilder : this.importedModuleBuilders.get(moduleName)) {
				String importedModuleName = importedModuleBuilder.getQualifiedName().toString();
				if(this.generatedModules.containsKey(importedModuleName)) {
					// Previous rounds
					importedModules.add(this.generatedModules.get(importedModuleName));
				}
				else if(this.importedModules.containsKey(importedModuleName)) {
					// compiled module
					importedModules.add(this.importedModules.get(importedModuleName));
				} 
				else if(this.faultyModules.contains(importedModuleName)) {
					// Faulty module
					roundFaultyModules.add(moduleName);
				}
				else if(this.moduleBuilders.containsValue(importedModuleBuilder)) {
					// Compiling Module
					generate = false;
					if(roundModules.containsKey(importedModuleName)) {
						importedModules.add(roundModules.get(importedModuleName));
					}
					else {
						ModuleInfo importedModule = this.generateModule(importedModuleBuilder, roundModules, roundGeneratedModules, roundFaultyModules);
						importedModules.add(importedModule);
					}
				}
				else {
					// Imported Module
					ModuleInfo importedModule = importedModuleBuilder.build();
					this.importedModules.put(importedModuleName, importedModule);
					importedModules.add(importedModule);
				}
			}
			moduleBuilder.modules(importedModules.toArray(new ModuleInfo[importedModules.size()]));
		}
		
		ModuleInfo moduleInfo = moduleBuilder.build();
		roundModules.put(moduleName, moduleInfo);
		if(moduleInfo.isFaulty()) {
			roundFaultyModules.add(moduleName);
		}

		if(generate && !moduleInfo.isFaulty()) {
			// Report
			System.out.println(moduleInfo.accept(this.moduleReporter, ""));
			
			// Generate class
			try {
				JavaFileObject moduleSourceFile = this.processingEnv.getFiler().createSourceFile(moduleInfo.getQualifiedName().getClassName(), this.moduleOriginatingElements.get(moduleName).stream().toArray(Element[]::new));
				try (Writer writer = moduleSourceFile.openWriter()) {
					writer.write(moduleInfo.accept(this.moduleClassGenerator, new ModuleClassGeneration(this.processingEnv, GenerationMode.MODULE_CLASS)));
					writer.flush();
				}
				
				System.out.println("Module " + moduleInfo.getQualifiedName() + " generated to " + moduleSourceFile.toUri());
//				this.processingEnv.getMessager().printMessage(Kind.NOTE, "Module " + moduleInfo.getQualifiedName() + " generated to " + moduleSourceFile.toUri());
			} catch (IOException e) {
				this.processingEnv.getMessager().printMessage(Kind.ERROR, "Error generating Module " + moduleInfo.getQualifiedName() + ": " + e.getMessage());
				e.printStackTrace();
			}
			roundGeneratedModules.put(moduleName, moduleInfo);
		}
		return moduleInfo;
	}
}
