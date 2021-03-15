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

/**
 * <p>
 * A wiring strategy is used to determine whether a bean can be plugged into a
 * socket.
 * </p>
 * 
 * @author <a href="mailto:jeremy.kuhn@winterframework.io">Jeremy Kuhn</a>
 *
 */
public interface WiringStrategy {

	/**
	 * <p>
	 * Determines whether the specified bean can be plugged into the specified
	 * socket.
	 * </p>
	 * 
	 * @param bean   a bean
	 * @param socket a socket
	 * 
	 * @return true if the bean is pluggable into the socket, false otherwise
	 */
	boolean isWirable(BeanInfo bean, SocketInfo socket);
}
