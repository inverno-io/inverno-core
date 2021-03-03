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
 * A single socket info represents a one-to-one relationship with a bean. It
 * defines an injection point for one single bean.
 * </p>
 * 
 * @author jkuhn
 *
 */
public interface SingleSocketInfo extends SocketInfo {

	/**
	 * <p>
	 * Returns the bean plugged into the socket.
	 * </p>
	 * 
	 * @return A bean.
	 */
	BeanInfo getBean();
}
