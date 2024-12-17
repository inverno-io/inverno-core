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
package io.inverno.core.compiler.spi;

import javax.lang.model.type.TypeMirror;

/**
 * <p>
 * A mutator bean info describes a mutator used to pre-process, decorate or completely transform a socket bean instance before it is made available for dependency injection.
 * </p>
 * 
 * <p>
 * A corresponding {@link SocketBeanInfo} is automatically created and exposed by the module.
 * </p>
 * 
 * @author <a href="jeremy.kuhn@inverno.io">Jeremy Kuhn</a>
 * @since 1.6
 */
public interface MutatorBeanInfo extends ModuleBeanInfo {

	/**
	 * <p>
	 * Returns the mutator type which is the type of the class transforming the mutated socket type into the actual bean type given by {@link BeanInfo#getType()}.
	 * </p>
	 * 
	 * @return the mutator type
	 */
	TypeMirror getMutatorType();

	/**
	 * <p>
	 * Returns the socket bean mutating the wired instance with the mutator.
	 * </p>
	 * 
	 * @return a mutating socket bean
	 */
	SocketBeanInfo getMutatingSocket();

	/**
	 * <p>
	 * Determines whether the mutator socket bean is always required.
	 * </p>
	 *
	 * <p>
	 * A required mutator bean is always invoked and a non-null instance always expected regardless on whether the bean is wired or not in the module.
	 * </p>
	 *
	 * @return true to ignore the socket, false otherwise
	 */
	boolean isRequired();
}
