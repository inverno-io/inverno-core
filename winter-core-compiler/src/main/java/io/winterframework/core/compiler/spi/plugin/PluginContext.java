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

import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;

import com.sun.source.util.DocTrees;

import io.winterframework.core.compiler.spi.CompilerOptions;

/**
 * <p>
 * A plugin context expose Java compiler utilities for operating on program
 * elements.
 * </p>
 * 
 * @author jkuhn
 * @since 1.1
 */
public interface PluginContext {

	/**
	 * <p>
	 * Returns the options passed as argument to the compiler.
	 * </p>
	 * 
	 * @return the compiler options
	 */
	CompilerOptions getOptions();
	
	/**
	 * <p>
	 * Returns Java compiler elements utility.
	 * </p>
	 * 
	 * @return the compiler elements utility
	 */
	Elements getElementUtils();
	
	/**
	 * <p>
	 * Returns Java compiler types utility.
	 * </p>
	 * 
	 * @return the compiler types utility
	 */
	Types getTypeUtils();
	
	/**
	 * <p>
	 * Returns Java compiler doc trees utility.
	 * </p>
	 * 
	 * @return
	 */
	DocTrees getDocUtils();
}
