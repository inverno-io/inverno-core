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
package io.winterframework.core.compiler.spi.plugin;

import java.util.Set;

import javax.lang.model.element.ModuleElement;

/**
 * <p>
 * A Compiler plugin provides a way to extend the Winter compiler in order to
 * process module beans or generate additional classes or resources during the
 * compilation of a module.
 * </p>
 * 
 * <p>
 * A plugin is only executed once during the compilation of a module right
 * before the module class is actually generated. If additional source files are
 * generated during the execution of the plugin, the generation of the module
 * class is postponed until the next round so that the Winter compiler can
 * integrate them.
 * </p>
 * 
 * @author jkuhn
 * @since 1.1
 *
 */
public interface CompilerPlugin {

	/**
	 * <p>
	 * Returns the list of annotation types supported by the plugin.
	 * </p>
	 * 
	 * <p>
	 * Note that this list can be empty if the plugin is only interested in
	 * processing the module beans.
	 * </p>
	 * 
	 * @return a list of annotation types
	 */
	default Set<String> getSupportedAnnotationTypes() {
		return Set.of();
	}
	
	/**
	 * <p>
	 * Returns the list of options supported by the plugin.
	 * </p>
	 * 
	 * @return a list of options
	 */
	default Set<String> getSupportedOptions() {
		return Set.of();
	}
	
	/**
	 * <p>
	 * Initializes the plugin with the specified context before execution.
	 * </p>
	 * 
	 * @param pluginContext the context used to initialize the plugin.
	 */
	void init(PluginContext pluginContext);
	
	/**
	 * <p>Determines whether the plugin can be executed for the specified module.</p>
	 * 
	 * <p>
	 * A plugin might not execute for several reasons, in such cases this method
	 * must return false. For instance when a module required by a generated class
	 * or a module providing a supported annotation is not declared in the compiled
	 * module descriptor, the plugin can't execute.
	 * </p>
	 * 
	 * @param moduleElement the module element
	 * 
	 * @return true if the plugin can be executed, false otherwise
	 */
	boolean canExecute(ModuleElement moduleElement);
	
	/**
	 * <p>
	 * Executes the plugin.
	 * </p>
	 * 
	 * @param execution the plugin execution
	 * @throws PluginExecutionException if something goes wrong during the execution
	 */
	void execute(PluginExecution execution) throws PluginExecutionException;
}
