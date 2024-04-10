/*
 * Copyright 2021 Jeremy KUHN
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
package io.inverno.core.compiler.spi;

import java.util.Optional;

/**
 * <p>
 * Exposes Inverno compiler options.
 * </p>
 * 
 * @author <a href="mailto:jeremy.kuhn@inverno.io">Jeremy Kuhn</a>
 *
 */
public interface CompilerOptions {

	/**
	 * <p>
	 * Indicates whether the compiler is verbose.
	 * </p>
	 * 
	 * @return true if the compiler is verbose, false otherwise
	 */
	boolean isVerbose();
	
	/**
	 * <p>
	 * Indicates whether the compiler displays debug information.
	 * </p>
	 * 
	 * @return true if the compiler is in debug mode, false otherwise
	 */
	boolean isDebug();
	
	/**
	 * <p>
	 * Determines whether the specified options has been set.
	 * </p>
	 * 
	 * @param name an option name
	 * @return true if the option has been set, false otherwise
	 */
	boolean containsOption(String name);
	
	/**
	 * <p>
	 * Returns the value of the specified option.
	 * </p>
	 *
	 * @param name an option name
	 *
	 * @return an optional returning the value or an empty optional if the options has not been set
	 */
	Optional<String> getOption(String name);
	
	/**
	 * <p>
	 * Determines whether the specified option is activated.
	 * </p>
	 *
	 * <p>
	 * An option can be a flag option (ie. with no value) in which case it is considered activated if it has been declared.
	 * </p>
	 *
	 * @param name              an option name
	 * @param defaultActivation whether the option is activated by default
	 *
	 * @return true if the option is activated, false otherwise
	 */
	boolean isOptionActivated(String name, boolean defaultActivation);
}
