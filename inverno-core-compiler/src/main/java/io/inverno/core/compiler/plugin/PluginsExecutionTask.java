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
package io.inverno.core.compiler.plugin;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.ModuleElement;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic.Kind;

import io.inverno.core.compiler.GenericCompilerOptions;
import io.inverno.core.compiler.spi.BeanInfo;
import io.inverno.core.compiler.spi.ModuleInfo;
import io.inverno.core.compiler.spi.ModuleQualifiedName;
import io.inverno.core.compiler.spi.plugin.CompilerPlugin;
import io.inverno.core.compiler.spi.plugin.PluginExecutionException;

/**
 * @author <a href="mailto:jeremy.kuhn@inverno.io">Jeremy Kuhn</a>
 *
 */
public class PluginsExecutionTask implements Callable<PluginsExecutionResult> {

	private final ProcessingEnvironment processingEnvironment;
	
	private final ModuleElement moduleElement;
	
	private final ModuleQualifiedName moduleQualifiedName;
	
	private final List<? extends BeanInfo> beans;
	
	private final List<? extends ModuleInfo> modules;
	
	private final GenericCompilerOptions options;
	
	private final Map<CompilerPlugin, Set<Element>> elementsByPlugins;
	
	PluginsExecutionTask(ProcessingEnvironment processingEnvironment, ModuleElement moduleElement, ModuleQualifiedName module, GenericCompilerOptions options, Set<? extends CompilerPlugin> plugins, List<? extends BeanInfo> beans, List<? extends ModuleInfo> modules) {
		this.processingEnvironment = processingEnvironment;
		this.moduleElement = moduleElement;
		this.moduleQualifiedName = module;
		this.beans = beans;
		this.modules = modules;
		this.options = options;
		this.elementsByPlugins = plugins.stream().collect(Collectors.toMap(Function.identity(), plugin -> new HashSet<>()));
	}

	public ModuleQualifiedName getModule() {
		return this.moduleQualifiedName;
	}
	
	public void addRound(RoundEnvironment roundEnv) {
		this.elementsByPlugins.entrySet().forEach(entry -> {
			for(String supportedAnnotationType : entry.getKey().getSupportedAnnotationTypes()) {
				// This can be null when there is missing dependencies (typically the module
				// provided the supported annotation is just not required in the compiled
				// module). 
				// In such case the plugin execution will be skipped later in the process.
				TypeElement pluginAnnotationTypeElement = this.processingEnvironment.getElementUtils().getTypeElement(supportedAnnotationType);
				if(pluginAnnotationTypeElement != null) {
					entry.getValue().addAll(roundEnv.getElementsAnnotatedWith(pluginAnnotationTypeElement).stream()
						.map(element -> {
							ModuleElement currentModuleElement = this.processingEnvironment.getElementUtils().getModuleOf(element);
							if(currentModuleElement == null) {
								// We exclude elements coming from the unnamed module 
								return null;
							}
							if(!currentModuleElement.getQualifiedName().toString().equals(this.moduleQualifiedName.toString())) {
								// We only consider elements from the module we are trying to generate
								return null;
							}
							return element;
						})
						.filter(Objects::nonNull)
						.collect(Collectors.toSet())
					);
				}
			}
		});
	}
	
	@Override
	public PluginsExecutionResult call() {
		if(this.options.isVerbose()) {
			System.out.println("Executing plugins for module " + this.moduleQualifiedName + "...");
		}
		PluginsExecutionResult result = new PluginsExecutionResult(this.elementsByPlugins.entrySet().stream()
			.map(entry -> {
				if(this.options.isVerbose()) {
					System.out.print(" - " + entry.getKey().getClass().getCanonicalName() + " (" + entry.getValue().size() + " elements)... ");
				}
				if(entry.getKey().canExecute(this.moduleElement)) {
					GenericPluginExecution execution = new GenericPluginExecution(this.processingEnvironment, this.moduleElement, this.moduleQualifiedName, entry.getValue(), this.beans, this.modules);
					try {
						// We want to execute a plugin even if annotated elements are not considered since we also want to process module beans
						entry.getKey().execute(execution);
						if(this.options.isVerbose()) {
							if(execution.hasError()) {
								System.out.println("[  KO  ]");
							}
							else {
								System.out.println("[  OK  ]");
							}
							if(execution.hasGeneratedSourceFiles()) {
								System.out.println(execution.getGeneratedSourceFiles().stream().map(source -> "     - " + source.toUri().toString()).collect(Collectors.joining("\n")));
							}
							if(execution.hasGeneratedResourceFiles()) {
								System.out.println(execution.getGeneratedResourceFiles().stream().map(source -> "     - " + source.toUri().toString()).collect(Collectors.joining("\n")));
							}
						}
					}
					catch (PluginExecutionException e) {
						execution.setFailed(true);
						if(this.options.isVerbose()) {
							System.out.println("[  KO  ]");
						}
						this.processingEnvironment.getMessager().printMessage(Kind.MANDATORY_WARNING, "Error executing plugin " + entry.getKey().getClass() + " for module " + this.moduleQualifiedName + ": " + e.getMessage());
						if(this.options.isDebug()) {
							e.printStackTrace();
						}
					}
					catch (Throwable t) {
						execution.setFailed(true);
						if(this.options.isVerbose()) {
							System.out.println("[  KO  ]");
						}
						this.processingEnvironment.getMessager().printMessage(Kind.MANDATORY_WARNING, "Fatal error executing plugin " + entry.getKey().getClass() + " for module " + this.moduleQualifiedName);
						t.printStackTrace();
					}
					return execution;
				}
				else {
					if(this.options.isVerbose()) {
						System.out.println("[ SKIP ]");
					}
					return null;
				}
				
			})
			.filter(Objects::nonNull)
			.collect(Collectors.toList())
		);
		if(this.options.isVerbose()) {
			System.out.println();
		}
		return result;
	}
}
