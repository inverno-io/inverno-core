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
 * A bean socket represents a bean dependency, a bean required or desirable by a
 * bean to operate properly. Sockets are basically defined to connect beans
 * together through dependency injection. By convention, constructor arguments
 * and setter methods are implicit bean sockets. This annotation must be used on
 * a constructor or a method to make it explicit in case of ambiguities.
 * </p>
 * 
 * <p>
 * An ambiguity arises when a bean defines more than one constructor which can
 * be removed by annotating the right constructor.
 * </p>
 * 
 * <p>
 * Ambiguities can also arise when some setter methods must not be considered as
 * bean sockets. In that case, you must explicitly annotate the setter methods
 * to consider.
 * </p>
 * 
 * @author <a href="mailto:jeremy.kuhn@winterframework.io">Jeremy Kuhn</a>
 * @since 1.0
 */
@Retention(RetentionPolicy.CLASS)
@Target({ ElementType.CONSTRUCTOR, ElementType.METHOD })
public @interface BeanSocket {

}
