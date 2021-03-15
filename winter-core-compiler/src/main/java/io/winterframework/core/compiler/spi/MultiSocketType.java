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
 * The multiple socket type used in {@link MultiSocketInfo} as injection types.
 * </p>
 * 
 * <p>
 * Multiple beans can be plugged in a multi socket info as an array, a
 * collection, a set or a list.
 * </p>
 * 
 * @author <a href="mailto:jeremy.kuhn@winterframework.io">Jeremy Kuhn</a>
 *
 */
public enum MultiSocketType {

	/**
	 * <p>
	 * Designates an array multiple socket.
	 * </p>
	 */
	ARRAY,
	/**
	 * <p>
	 * Designates a collection multiple socket.
	 * </p>
	 */
	COLLECTION,
	/**
	 * <p>
	 * Designates a set multiple socket.
	 * </p>
	 */
	SET,
	/**
	 * <p>
	 * Designates a list multiple socket.
	 * </p>
	 */
	LIST;
}
