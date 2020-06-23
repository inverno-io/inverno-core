/*
 * Copyright 2018 Jeremy KUHN
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
package io.winterframework.core.annotation;

import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.RetentionPolicy.CLASS;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * <p>
 * Used on a module builder class to specify how the socket beans of a module
 * are wired to module beans and/or imported module beans. This is actually
 * necessary to be able to import required modules beans in a module while
 * preventing dependency cycles.
 * </p>
 * 
 * <p>
 * This annotation has to be exposed in the API for the module to compile but it
 * is only useful to the Winter compiler in generate module classes and as a
 * result should never be used in the development of a module.
 * </p>
 * 
 * @author jkuhn
 * @since 1.0
 */
@Retention(CLASS)
@Target(PARAMETER)
public @interface WiredTo {

	/**
	 * Indicates the name of the module beans a socket bean is wired to.
	 * 
	 * @return A list of beans
	 */
	String[] value();
}
