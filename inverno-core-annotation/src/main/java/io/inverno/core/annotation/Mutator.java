/*
 * Copyright 2024 Jeremy Kuhn
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
package io.inverno.core.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * <p>
 * Used in conjunction with {@link Bean @Bean} to declare a mutating socket bean.
 * </p>
 * 
 * <p>
 * A mutating socket bean must implement {@link java.util.function.Function Function<A, B>} where {@code A} is the type of the socket exposed by the module and {@code B} is the type of the instance
 * after transformation and used during dependency injection.
 * </p>
 * 
 * <pre>{@code
 * @Mutator @Bean
 * public class MutatingSocketBean implements Function<Type, TransformedType> {
 * 
 *     @Override
 *     public TransformedType apply(Type instance) {...}
 * }
 * }</pre>
 * 
 * <p>
 * Such socket can be created when there is a need to pre-process, adapt, decorate or transform an external dependency to fit module's needs.
 * </p>
 * 
 * @author <a href="jeremy.kuhn@inverno.io">Jeremy Kuhn</a>
 * @since 1.6
 * 
 * @see Bean
 */
@Retention(RetentionPolicy.CLASS)
@Target({ ElementType.TYPE })
public @interface Mutator {

	/**
	 * <p>
	 * Indicates whether the socket should be required and mutator always invoked regardless on whether the the socket is wired or not.
	 * </p>
	 *
	 * @return true to make the socket required and always invoke the mutator on the wired instance, false otherwise
	 */
	boolean required() default false;
}
