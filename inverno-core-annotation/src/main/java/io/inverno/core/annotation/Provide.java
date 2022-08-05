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
package io.inverno.core.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * <p>
 * Specifies the type provided by a bean, defaulting to the actual bean type.
 * </p>
 *
 * <p>
 * This allows to control how a bean is actually exposed. For instance, you might not want to expose the actual bean type which is most likely an implementation class not exported by the module and
 * therefore not accessible to external Java modules anyway, you'd rather choose to expose a public class or an interface extended or implemented by the bean class.
 * </p>
 *
 * <p>
 * This annotation can be either specified on the {@link Bean} annotated type or on one of its a super type. In the first case, the provided type is specified by the annotation value, in the second
 * case, the provided type is the annotated direct super type, the annotation value being ignored. Defining a type incompatible with the actual bean type or Specifying the annotation multiple times
 * will result in a compilation errors.
 * </p>
 *
 * <p>
 * For example, the following bean will be exposed as <code>SomeService</code> outside the module. A bean can only provide one single type.
 * </p>
 *
 * <pre>{@code
 * @Bean
 * @Provide(SomeService.class)
 * public class ModuleBean implements SomeService, SomeOtherService {
 *
 * }
 * }</pre>
 *
 * <p>
 * which is equivalent to:
 * </p>
 *
 * <pre>{@code
 * @Bean
 * public class ModuleBean implements @Provide SomeService, SomeOtherService {
 *
 * }
 * }</pre>
 *
 * <p>
 * Note that this also has an impact on bean wiring. From within the module the bean provides its actual visible type which can then be wired to any assignable socket. From outside the module, wiring
 * is only based on the provided type. There is however one exception when the annotation is used on an overridable bean, the wired type inside and outside the module is always the provided type.
 * </p>
 *
 * @author <a href="mailto:jeremy.kuhn@inverno.io">Jeremy Kuhn</a>
 * @since 1.0
 *
 * @see Bean
 */
@Retention(RetentionPolicy.CLASS)
@Target({ ElementType.TYPE, ElementType.TYPE_USE })
public @interface Provide {

	/**
	 * <p>
	 * Specifies the type provided by the bean.
	 * </p>
	 * 
	 * <p>
	 * This type must be compatible with the actual bean type.
	 * </p>
	 * 
	 * @return A type
	 */
	Class<?> value() default Object.class;
}
