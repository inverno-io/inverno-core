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
package io.winterframework.core.compiler.spi;

/**
 * 
 * <p>
 * A reporter info is used to report and track info, warning and error on
 * module's elements and annotations during compilation.
 * </p>
 * 
 * @author jkuhn
 *
 */
public interface ReporterInfo {

	/**
	 * <p>
	 * Indicates whether this info has errors.
	 * </p>
	 * 
	 * @return true if there are errors, false otherwise
	 */
	boolean hasError();

	/**
	 * <p>
	 * Indicates whether this info has warnings.
	 * </p>
	 * 
	 * @return true if there are warnings, false otherwise
	 */
	boolean hasWarning();

	/**
	 * <p>
	 * Reports an error on this info.
	 * </p>
	 * 
	 * @param message the message to report
	 */
	void error(String message);

	/**
	 * <p>
	 * Reports a warning on this info.
	 * </p>
	 * 
	 * @param message the message to report
	 */
	void warning(String message);
}
