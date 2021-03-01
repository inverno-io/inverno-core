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

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;

import com.sun.source.util.DocTrees;

import io.winterframework.core.compiler.GenericCompilerOptions;
import io.winterframework.core.compiler.spi.CompilerOptions;
import io.winterframework.core.compiler.spi.plugin.PluginContext;

/**
 * @author jkuhn
 *
 */
class GenericPluginContext implements PluginContext {

	private ProcessingEnvironment processingEnvironment;
	private GenericCompilerOptions options;
	
	private DocTrees docTrees;
	
	public GenericPluginContext(ProcessingEnvironment processingEnvironment, GenericCompilerOptions options) {
		this.processingEnvironment = processingEnvironment;
		this.options = options;
	}

	@Override
	public CompilerOptions getOptions() {
		return this.options;
	}
	
	@Override
	public Elements getElementUtils() {
		return this.processingEnvironment.getElementUtils();
	}

	@Override
	public Types getTypeUtils() {
		return this.processingEnvironment.getTypeUtils();
	}
	
	@Override
	public DocTrees getDocUtils() {
		if(this.docTrees == null) {
			this.docTrees = DocTrees.instance(this.processingEnvironment);
		}
		return this.docTrees;
	}
}
