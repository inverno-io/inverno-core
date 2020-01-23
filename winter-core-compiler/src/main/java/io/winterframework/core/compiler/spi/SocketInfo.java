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
package io.winterframework.core.compiler.spi;

import java.util.Optional;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.type.TypeMirror;

import io.winterframework.core.annotation.Selector;

/**
 * <p>
 * Base socket info interface specifying data and services common to all info.
 * </p>
 * 
 * <p>
 * A socket represents an injection point for a single bean or multiple beans on
 * modules or beans.
 * </p>
 * 
 * @author jkuhn
 *
 */
public interface SocketInfo extends Info {

	/**
	 * <p>
	 * Returns the type of bean that can be plugged into the socket.
	 * </p>
	 * 
	 * @return A type.
	 */
	TypeMirror getType();
	
	/**
	 * <p>
	 * Returns the executable element to use to plug beans into the socket.
	 * </p>
	 * 
	 * <p>Note that this is not applicable to socket beans in a module being generated.</p>
	 * 
	 * @return An optional providing the executable element or an empty optional if not applicable.
	 */
	Optional<ExecutableElement> getSocketElement();
	
	/**
	 * <p>
	 * Returns the {@link Selector} annotations defined on the socket.
	 * </p>
	 * 
	 * @return An array of selector annotations.
	 */
	AnnotationMirror[] getSelectors();
	
	/**
	 * <p>
	 * Determines whether the socket is optional.
	 * </p>
	 * 
	 * <p>
	 * An optional socket is not required to be resolved for a module to operate
	 * properly.
	 * </p>
	 * 
	 * @return true if the socket is optional, false otherwise.
	 */
	boolean isOptional();
	
	/**
	 * <p>
	 * Determines whether the socket is resolved.
	 * </p>
	 * 
	 * <p>
	 * A socket is resolved if the dependency it represents has been resolved (ie. a
	 * bean has been plugged into the socket).
	 * </p>
	 * 
	 * @return true if the socket is resolved, false otherwise.
	 */
	boolean isResolved();
}
