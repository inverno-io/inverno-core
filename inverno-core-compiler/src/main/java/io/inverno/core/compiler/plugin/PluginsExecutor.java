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

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.ServiceLoader;
import java.util.Set;
import java.util.stream.Collectors;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.ModuleElement;

import io.inverno.core.compiler.GenericCompilerOptions;
import io.inverno.core.compiler.InvernoCompiler;
import io.inverno.core.compiler.spi.BeanInfo;
import io.inverno.core.compiler.spi.ModuleInfo;
import io.inverno.core.compiler.spi.ModuleQualifiedName;
import io.inverno.core.compiler.spi.plugin.CompilerPlugin;

/**
 * @author <a href="mailto:jeremy.kuhn@inverno.io">Jeremy Kuhn</a>
 *
 */
public class PluginsExecutor {

	private final ProcessingEnvironment processingEnvironment;
	
	private final GenericCompilerOptions options;
	
	private Set<CompilerPlugin> plugins;
	
	private Map<ModuleQualifiedName, PluginsExecutionTask> executionByModule;
	
	public PluginsExecutor(ProcessingEnvironment processingEnvironment, GenericCompilerOptions options) {
		this.processingEnvironment = processingEnvironment;
		this.options = options;
		this.executionByModule = new HashMap<>();
		
		this.loadPlugins();
	}

	private void loadPlugins() {
		ServiceLoader<CompilerPlugin> loader;
		if(InvernoCompiler.class.getModule().isNamed()) {
			// --processor-module-path
			loader = ServiceLoader.load(InvernoCompiler.class.getModule().getLayer(), CompilerPlugin.class);
		}
		else {
			// --processor-path
			// requires provider configuration files
			loader = ServiceLoader.load(CompilerPlugin.class, PluginsExecutor.class.getClassLoader());
		}
		
		this.plugins = loader.stream().map(provider -> {
			CompilerPlugin plugin = provider.get();
			plugin.init(new GenericPluginContext(this.processingEnvironment, this.options.withFilter(name -> plugin.getSupportedOptions() != null && plugin.getSupportedOptions().contains(name))));
			return plugin;
		})
		.filter(Objects::nonNull)
		.collect(Collectors.toSet());
	}
	
	public Set<CompilerPlugin> getPlugins() {
		return plugins;
	}
	
	public PluginsExecutionTask getTask(ModuleElement moduleElement, ModuleQualifiedName moduleQualifiedName, List<? extends BeanInfo> beans, List<? extends ModuleInfo> modules) {
		if(!this.executionByModule.containsKey(moduleQualifiedName)) {
			this.executionByModule.put(moduleQualifiedName, new PluginsExecutionTask(this.processingEnvironment, moduleElement, moduleQualifiedName, this.options, this.plugins, beans, modules));
		}
		return this.executionByModule.get(moduleQualifiedName);
	}
}
