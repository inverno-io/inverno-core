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
import java.util.function.Supplier;

/**
 * <p>
 * Used in conjunction with {@link Bean} to indicate a wrapper bean.
 * </p>
 * 
 * <p>
 * A wrapper bean should be created when there's a need to expose legacy code
 * which can't be annotated with {@link Bean}, it allows to delegate the actual
 * instantiation to a {@link Supplier} instead of a bean constructor. As a
 * result a wrapper bean must implements {@link Supplier}.
 * </p>
 * 
 * <p>
 * A wrapper bean follows the same rules as a regular bean: dependencies are
 * injected into sockets defined on the wrapper bean, initialization methods are
 * invoked after dependency injection and destroy methods before bean removal.
 * However the instance actually exposed to the container is the result of
 * {@link Supplier#get()}. As any wrapper, a wrapper bean wrap a single
 * instance, so this method should always return the same instance.
 * </p>
 * 
 * @author jkuhn
 * @Since 1.0
 * @see Bean
 */
@Retention(RetentionPolicy.CLASS)
@Target({ ElementType.TYPE })
@Bean
public @interface Wrapper {

}
