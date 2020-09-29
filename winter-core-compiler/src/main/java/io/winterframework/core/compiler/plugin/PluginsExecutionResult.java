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

import java.util.List;
import java.util.stream.Collectors;

import javax.tools.JavaFileObject;

/**
 * @author jkuhn
 *
 */
public class PluginsExecutionResult {

	private List<GenericPluginExecution> pluginExecutions;
	
	public PluginsExecutionResult(List<GenericPluginExecution> pluginExecutions) {
		this.pluginExecutions = pluginExecutions;
	}

	public boolean hasError() {
		return this.pluginExecutions.stream().anyMatch(GenericPluginExecution::hasError);
	}
	
	public boolean hasGeneratedSourceFiles() {
		return this.pluginExecutions.stream().anyMatch(execution -> execution.getGeneratedSourceFiles().size() > 0);
	}
	
	public List<JavaFileObject> getGeneratedSourceFiles() {
		return this.pluginExecutions.stream().flatMap(execution -> execution.getGeneratedSourceFiles().stream()).collect(Collectors.toList());
	}
}
