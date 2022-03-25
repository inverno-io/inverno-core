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
package io.inverno.core.compiler.spi;

import javax.lang.model.type.TypeMirror;

/**
 * <p>
 * Base socket bean interface.
 * </p>
 * 
 * <p>
 * A socket bean represents an injection point in a module for beans of type
 * assignable to the socket type.
 * </p>
 * 
 * @author <a href="mailto:jeremy.kuhn@inverno.io">Jeremy Kuhn</a>
 *
 */
public interface SocketBeanInfo extends BeanInfo, SocketInfo {

	/**
	 * <p>
	 * Returns the actual type of the socket bean.
	 * </p>
	 * 
	 * <p>
	 * This type should not be confused with the type returned by
	 * {@link SocketInfo#getType()}: the socket type is the type of the interface in
	 * a module defining the socket whereas the type is the type of bean that can be
	 * plugged into the socket.
	 * </p>
	 * 
	 * @return a type
	 */
	TypeMirror getSocketType();

	/**
	 * <p>
	 * Returns the qualified names of all the beans defined in the enclosing
	 * directly or indirectly wired to the socket.
	 * </p>
	 * 
	 * @return an array of bean qualified names
	 */
	BeanQualifiedName[] getWiredBeans();
	
	/**
	 * <p>
	 * Determines whether the socket bean is wired within the module.
	 * </p>
	 * 
	 * <p>
	 * A socket can be wired to a bean defined in the enclosing module or to a
	 * socket bean defined in a component module.
	 * </p>
	 * 
	 * @return true if the socket is a wired within the module, false otherwise
	 */
	boolean isWired();
}
