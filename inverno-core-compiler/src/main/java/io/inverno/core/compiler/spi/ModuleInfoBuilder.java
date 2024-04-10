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

import javax.lang.model.element.ModuleElement;

/**
 * <p>
 * A module info builder is used to create Module info from beans, sockets and required modules.
 * </p>
 * 
 * @author <a href="mailto:jeremy.kuhn@inverno.io">Jeremy Kuhn</a>
 *
 */
public interface ModuleInfoBuilder {

	/**
	 * <p>
	 * Returns the module element.
	 * </p>
	 * 
	 * @return A module element
	 */
	ModuleElement getElement();
	
	/**
	 * <p>
	 * Returns the qualified name of the module being build by the builder.
	 * </p>
	 * 
	 * @return a module qualified name
	 */
	ModuleQualifiedName getQualifiedName();

	/**
	 * <p>
	 * Sets the beans that should be part of the module.
	 * </p>
	 * 
	 * @param beans an array of bean info
	 * 
	 * @return the module info builder
	 */
	ModuleInfoBuilder beans(ModuleBeanInfo[] beans);

	/**
	 * <p>
	 * Sets the sockets that should be part of the module.
	 * </p>
	 *
	 * @param sockets an array of socket info
	 *
	 * @return the module info builder
	 */
	ModuleInfoBuilder sockets(SocketBeanInfo[] sockets);
	
	/**
	 * <p>
	 * Sets the modules that should be part of the module.
	 * </p>
	 * 
	 * @param modules an array of module info
	 * 
	 * @return the module info builder
	 */
	ModuleInfoBuilder modules(ModuleInfo[] modules);

	/**
	 * <p>
	 * Builds the module.
	 * </p>
	 * 
	 * @return a module info
	 */
	ModuleInfo build();
}
