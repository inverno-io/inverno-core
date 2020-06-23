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
 * A multi socket info represents a one-to-many relationship with multiple
 * beans. It defines an injection point for an array, a set, a collection or a
 * list of beans.
 * </p>
 * 
 * @author jkuhn
 *
 */
public interface MultiSocketInfo extends SocketInfo {

	/**
	 * <p>
	 * Returns the beans plugged into the socket.
	 * </p>
	 * 
	 * @return An array of beans.
	 */
	BeanInfo[] getBeans();

	/**
	 * <p>
	 * Returns the multiple type of the socket.
	 * </p>
	 * 
	 * @return A multiple socket type
	 */
	MultiSocketType getMultiType();
}
