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

import java.lang.annotation.ElementType;
import java.lang.annotation.Target;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;
import java.util.ServiceLoader;
import java.util.Set;
import java.util.stream.Collectors;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import javax.tools.Diagnostic.Kind;

import io.winterframework.core.compiler.WinterCompiler;
import io.winterframework.core.compiler.spi.ModuleQualifiedName;
import io.winterframework.core.compiler.spi.plugin.CompilerPlugin;

/**
 * @author jkuhn
 *
 */
public class PluginsExecutor {

	private ProcessingEnvironment processingEnvironment;
	
	private WinterCompiler.Options options;
	
	private GenericPluginContext compilerPluginContext;
	
	private Set<CompilerPlugin> plugins;
	
	private Map<ModuleQualifiedName, PluginsExecutionTask> executionByModule;
	
	public PluginsExecutor(ProcessingEnvironment processingEnvironment, WinterCompiler.Options options) {
		this.processingEnvironment = processingEnvironment;
		this.options = options;
		this.executionByModule = new HashMap<>();
		
		this.loadPlugins();
	}

	@SuppressWarnings("unchecked")
	private void loadPlugins() {
		ServiceLoader<CompilerPlugin> loader;
		if(WinterCompiler.class.getModule().isNamed()) {
			// --processor-module-path
			loader = ServiceLoader.load(WinterCompiler.class.getModule().getLayer(), CompilerPlugin.class);
		}
		else {
			// --processor-path
			// requires provider configuration files
			loader = ServiceLoader.load(CompilerPlugin.class, PluginsExecutor.class.getClassLoader());
		}
		
		TypeMirror targetAnnotationType = this.processingEnvironment.getElementUtils().getTypeElement(Target.class.getCanonicalName()).asType();
		
		this.compilerPluginContext = new GenericPluginContext(processingEnvironment);
		this.plugins = loader.stream().map(provider -> {
			CompilerPlugin plugin = provider.get();
			TypeElement pluginAnnotationElement = this.processingEnvironment.getElementUtils().getTypeElement(plugin.getSupportedAnnotationType());
			Optional<? extends AnnotationMirror> targetAnnotation = pluginAnnotationElement.getAnnotationMirrors().stream().filter(annotation -> this.processingEnvironment.getTypeUtils().isSameType(annotation.getAnnotationType(), targetAnnotationType)).findFirst();
			if(!targetAnnotation.isPresent()) {
				return null;
			}
			
			String[] targetTypes = null;
			for(Entry<? extends ExecutableElement, ? extends AnnotationValue> entry : this.processingEnvironment.getElementUtils().getElementValuesWithDefaults(targetAnnotation.get()).entrySet()) {
				switch(entry.getKey().getSimpleName().toString()) {
					case "value" : targetTypes = ((List<AnnotationValue>)entry.getValue().getValue()).stream().map(v -> v.getValue().toString()).toArray(String[]::new);
						break;
				}
			}
			if(targetTypes == null || targetTypes.length > 1 || !targetTypes[0].equals(ElementType.TYPE.toString())) {
				this.processingEnvironment.getMessager().printMessage(Kind.MANDATORY_WARNING, "Ignoring Plugin " + plugin.getClass() + " which must target " + ElementType.TYPE + " only");
			}
			
			plugin.init(this.compilerPluginContext);
			return plugin;
		})
		.filter(Objects::nonNull)
		.collect(Collectors.toSet());
	}
	
	public Set<CompilerPlugin> getPlugins() {
		return plugins;
	}
	
	public PluginsExecutionTask getTask(ModuleQualifiedName module) {
		if(!this.executionByModule.containsKey(module)) {
			this.executionByModule.put(module, new PluginsExecutionTask(module, this.processingEnvironment, this.options, this.plugins));
		}
		return this.executionByModule.get(module);
	}
}
