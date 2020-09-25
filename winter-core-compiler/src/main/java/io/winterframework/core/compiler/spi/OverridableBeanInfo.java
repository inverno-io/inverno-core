/*
 * Copyright 2020 Jeremy KUHN
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
 * An overridable bean info holds the data required to process an overridable bean in a
 * module.
 * </p>
 * 
 * @author jkuhn
 * 
 */
public interface OverridableBeanInfo extends ModuleBeanInfo {

	/**
	 * <p>
	 * Returns the module bean possibly overridden by the overriding socket.
	 * </p>
	 * 
	 * @return A module bean
	 */
	ModuleBeanInfo getOverridableBean();
	
	/**
	 * <p>
	 * Returns the socket bean overriding the overridable bean.
	 * </p>
	 * 
	 * @return An overriding socket
	 */
	OverridingSocketBeanInfo getOverridingSocket();
}
