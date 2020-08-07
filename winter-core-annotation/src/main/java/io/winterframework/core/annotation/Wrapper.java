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
import java.lang.ref.WeakReference;
import java.util.function.Supplier;

/**
 * <p>
 * Used in conjunction with {@link Bean} to indicate a wrapper bean.
 * </p>
 * 
 * <p>
 * A wrapper bean should be used to create beans using legacy code which can't
 * be annotated with {@link Bean}, it allows to delegate the actual
 * instantiation, initialization and destruction to a wrapper class. As a result
 * a wrapper implementing the {@link Supplier} interface.
 * </p>
 * 
 * <p>
 * A wrapper bean follows the same rules as regular beans: dependencies are
 * injected into sockets defined on the wrapper class, initialization methods
 * are invoked after dependency injection and destroy methods before bean
 * removal on the wrapper instance. However the instance actually exposed is
 * returned by the {@link Supplier#get()} method. There is no requirement that a
 * new or distinct result be returned each time the wrapper is invoked but when
 * initialization or destruction methods are specified, the wrapper must
 * naturally create and always return a single wrapped instance which can then
 * be initialized and destroyed by the wrapper. In that particular case,
 * {@link WeakReference} should be used in the wrapper to hold the actual bean
 * instance to prevent memory leaks that might arise in certain situations.
 * </p>
 * 
 * @author jkuhn
 * @since 1.0
 * 
 * @see Bean
 */
@Retention(RetentionPolicy.CLASS)
@Target({ ElementType.TYPE })
public @interface Wrapper {

}
