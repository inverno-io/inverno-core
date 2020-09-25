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
package io.winterframework.core.compiler.plugin;

import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.ModuleElement;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic.Kind;

import io.winterframework.core.compiler.WinterCompiler;
import io.winterframework.core.compiler.spi.ModuleQualifiedName;
import io.winterframework.core.compiler.spi.plugin.CompilerPlugin;
import io.winterframework.core.compiler.spi.plugin.PluginExecutionException;

/**
 * @author jkuhn
 *
 */
public class PluginsExecutionTask implements Callable<PluginsExecutionResult> {

	private ModuleQualifiedName module;
	
	private ProcessingEnvironment processingEnvironment;
	
	private WinterCompiler.Options options;
	
	private Map<CompilerPlugin, Set<TypeElement>> elementsByPlugins;
	
	/**
	 * 
	 */
	PluginsExecutionTask(ModuleQualifiedName module, ProcessingEnvironment processingEnvironment, WinterCompiler.Options options, Set<? extends CompilerPlugin> plugins) {
		this.module = module;
		this.processingEnvironment = processingEnvironment;
		this.options = options;
		this.elementsByPlugins = plugins.stream().collect(Collectors.toMap(Function.identity(), plugin -> new HashSet<>()));
	}

	public ModuleQualifiedName getModule() {
		return this.module;
	}
	
	public void addRound(RoundEnvironment roundEnv) {
		this.elementsByPlugins.entrySet().forEach(entry -> {
			TypeElement pluginAnnotationTypeElement = this.processingEnvironment.getElementUtils().getTypeElement(entry.getKey().getSupportedAnnotationType());
			entry.getValue().addAll(roundEnv.getElementsAnnotatedWith(pluginAnnotationTypeElement).stream()
				.map(element -> {
					if(!(element instanceof TypeElement)) {
						// Plugins only consider type element
						return null;
					}
					ModuleElement moduleElement = this.processingEnvironment.getElementUtils().getModuleOf(element);
					if(moduleElement == null) {
						// We exclude elements coming from the unnamed module 
						return null;
					}
					if(!moduleElement.getQualifiedName().toString().equals(this.module.toString())) {
						// We only consider elements from the module we are trying to generate
						return null;
					}
					return (TypeElement)element;
				})
				.filter(Objects::nonNull)
				.collect(Collectors.toSet())
			);
		});
	}
	
	public PluginsExecutionResult call() {
		if(this.options.isVerbose()) {
			System.out.println("Executing plugins for module " + this.module + "...");
		}
		PluginsExecutionResult result = new PluginsExecutionResult(this.elementsByPlugins.entrySet().stream()
			.map(entry -> {
				GenericPluginExecution execution = new GenericPluginExecution(this.processingEnvironment, this.module, entry.getValue());
				try {
					if(this.options.isVerbose()) {
						System.out.print(" - " + entry.getKey().getClass().getCanonicalName() + " (" + entry.getValue().size() + " elements)... ");
					}
					if(entry.getValue().size() > 0) {
						entry.getKey().execute(execution);
						if(this.options.isVerbose()) {
							if(execution.hasError()) {
								System.out.println("[  KO  ]");
							}
							else {
								System.out.println("[  OK  ]");
							}
							System.out.println(execution.getGeneratedSourceFiles().stream().map(source -> "     - " + source.toUri().toString()).collect(Collectors.joining("\n")));
						}
					}
					else {
						if(this.options.isVerbose()) {
							System.out.println("[ SKIP ]");
						}
					}
					
				}
				catch (PluginExecutionException e) {
					execution.setFailed(true);
					if(this.options.isVerbose()) {
						System.out.println("[  KO  ]");
					}
					this.processingEnvironment.getMessager().printMessage(Kind.MANDATORY_WARNING, "Error executing plugin " + entry.getKey().getClass() + " for module " + this.module + ": " + e.getMessage());
					if(this.options.isDebug()) {
						e.printStackTrace();
					}
				}
				catch (Throwable t) {
					execution.setFailed(true);
					if(this.options.isVerbose()) {
						System.out.println("[  KO  ]");
					}
					this.processingEnvironment.getMessager().printMessage(Kind.MANDATORY_WARNING, "Fatal error executing plugin " + entry.getKey().getClass() + " for module " + this.module);
					t.printStackTrace();
				}
				return execution;
			}).collect(Collectors.toList())
		);
		if(this.options.isVerbose()) {
			System.out.println();
		}
		return result;
	}
}
