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

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * <p>
 * A bean socket represents a bean dependency, a bean required or desirable by a bean to operate properly. Sockets are basically defined to connect beans together through dependency injection. By
 * convention, constructor arguments and setter methods are implicit bean sockets. This annotation allow to specify bean sockets in an explicit way. This is typically needed on a constructor or a 
 * method in case of ambiguities.
 * </p>
 *
 * <p>
 * An ambiguity arises when a bean defines more than one constructor which can be removed by annotating the right constructor which is the one that must be used to instantiate the bean and inject 
 * required dependencies.
 * </p>
 *
 * <p>
 * Ambiguities can also arise when some setter methods must not be considered as bean sockets. In that case, all setter methods that must be considered by the compiler must be annotated explicitly. 
 * This basically means that if no setter methods is annotated, all setter methods are implicitly considered but if at least one setter method is annotated, then only annotated methods shall be 
 * considered and all others ignored.
 * </p>
 * 
 * <p>
 * A bean socket can also be explicitly ignored by setting the {@link #enabled()} attribute to {@code false} in which case the annotated setter method or constructor will be simply ignored by the 
 * compiler.
 * </p>
 * 
 * <p>
 * The usage of {@code @BeanSocket} is exclusive, if it is specified, only annotated methods or constructors shall be considered. In case of constructor, exactly one constructor must be annotated 
 * with {@link #enabled()} set to {@code true}.
 * </p>
 * 
 * @author <a href="mailto:jeremy.kuhn@inverno.io">Jeremy Kuhn</a>
 * @since 1.0
 */
@Retention(RetentionPolicy.CLASS)
@Target({ ElementType.CONSTRUCTOR, ElementType.METHOD })
public @interface BeanSocket {

	/**
	 * <p>
	 * Explicitly marks a socket as enabled or disabled.
	 * </p>
	 * 
	 * <p>
	 * A disabled socket will be ignored by the compiler. This is set to {@code true} by default.
	 * </p>
	 * 
	 * @return true to explicitly enable a socket, false otherwise
	 */
	boolean enabled() default true;
}
