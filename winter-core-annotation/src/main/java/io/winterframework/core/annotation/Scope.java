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
 * Used in conjunction with {@link Bean} to indicate the bean life cycle.
 * </p>
 * 
 * <p>
 * For a {@link Type#SINGLETON} bean, one single instance is created and
 * injected in dependent beans. This is the default behavior when no scope is
 * specified.
 * </p>
 * 
 * <p>
 * For a {@link Type#PROTOTYPE} bean, a new instance is created when requested
 * which means every dependent beans receive a distinct instance.
 * </p>
 * 
 * <p>
 * Note that this annotation is irrelevant and therefore ignored when specified
 * on a socket bean
 * </p>
 * 
 * @author jkuhn
 * @since 1.0
 * 
 * @see Bean
 */
@Retention(RetentionPolicy.CLASS)
@Target({ ElementType.TYPE })
public @interface Scope {

	/**
	 * The possible scopes.
	 */
	public static enum Type {
		/**
		 * A singleton bean results in one single instance being created.
		 */
		SINGLETON,
		/**
		 * A prototype bean results in multiple instance being created when requested.
		 */
		PROTOTYPE
	}

	/**
	 * The bean scope which defaults to {@link Type#SINGLETON}.
	 */
	Type value() default Type.SINGLETON;
}
