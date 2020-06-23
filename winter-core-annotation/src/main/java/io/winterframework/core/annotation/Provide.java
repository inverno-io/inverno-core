/*
 * Copyright 2019 Jeremy KUHN
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
 * Specifies the type actually provided by a bean, defaulting to the actual bean
 * type.
 * </p>
 * 
 * <p>
 * This allows to control how a bean is actually exposed. For instance, you
 * might not want to expose the actual bean type which is most likely an
 * implementation class not exported by the module and therefore not accessible
 * to external Java modules anyway, you'd rather choose to expose a public class
 * or an interface extended or implemented by the bean class.
 * </p>
 * 
 * <p>
 * For example, the following bean will be exposed as <code>SomeService</code>
 * outside the module. A bean can only provide one single type.
 * </p>
 * 
 * <pre>
 *     &#64;Bean
 *     public class ModuleBean implements &#64;Provide SomeService, SomeOtherService {
 * 
 *     }
 * </pre>
 * 
 * <p>
 * Note that this also has an impact on bean wiring. From within the module the
 * bean provides its actual visible type which can then be wired to any
 * assignable socket. From outside the module, wiring is only based on the
 * provided type.
 * </p>
 * 
 * @author jkuhn
 * @since 1.0
 * 
 * @see Bean
 */
@Retention(RetentionPolicy.CLASS)
@Target({ ElementType.TYPE_USE })
public @interface Provide {

}
