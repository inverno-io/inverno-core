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
package io.inverno.core.annotation;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.MODULE;
import java.lang.annotation.Retention;
import static java.lang.annotation.RetentionPolicy.CLASS;
import java.lang.annotation.Target;

/**
 * <p>
 * Indicates a method that must be executed before a bean instance is destroyed when a module is stopped.
 * </p>
 *
 * <p>
 * Unlike Beans with scope {@link Bean.Strategy#SINGLETON}, beans with scope {@link Bean.Strategy#PROTOTYPE} might not be destroyed and therefore destroy methods not invoked when they are created
 * outside a module and dereferenced before the module is stopped. As a result you should generally avoid defining destroy methods on beans with scope prototype. If you have this kind of use case,
 * consider creating prototype beans that implement {@link AutoCloseable}, define the <code>close()</code> as destroy method, make sure it can be invoked twice, and create new instances as follows to
 * make sure instance are properly destroyed:
 * </p>
 *
 * <pre>{@code
 * try (MyPrototype instance = myModuleInstance.myPrototype()) {
 *     ...
 * }
 * }</pre>
 *
 * @author <a href="mailto:jeremy.kuhn@inverno.io">Jeremy Kuhn</a>
 * @since 1.0
 */
@Retention(CLASS)
@Target({ METHOD, MODULE })
public @interface Destroy {

}
