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

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * <p>
 * Indicates that a module is a winter module that must be processed and a
 * module class generated during compilation .
 * </p>
 * 
 * <p>
 * By default, the name of the generated class is the name of the module. In
 * order for the module to be functional its package must be exported by the
 * module.
 * </p>
 * 
 * <p>
 * In case of name conflict, it is possible to explicitly specify the name of
 * the generated class.s
 * </p>
 * 
 * @author jkuhn
 * @since 1.0
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.MODULE })
public @interface Module {

	/**
	 * <p>
	 * Indicates the name of the module's generated class, defaults to the name of
	 * the module.
	 * </p>
	 */
	String className() default "";
}
