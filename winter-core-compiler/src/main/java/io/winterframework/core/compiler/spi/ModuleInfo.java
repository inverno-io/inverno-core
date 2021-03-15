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

import javax.lang.model.element.ModuleElement;

/**
 * <p>
 * A module info holds the data required to process a module.
 * </p>
 * 
 * @author <a href="mailto:jeremy.kuhn@winterframework.io">Jeremy Kuhn</a>
 *
 */
public interface ModuleInfo extends Info {

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
	 * Returns a module qualified name.
	 * </p>
	 * 
	 * @return a module qualified name
	 */
	ModuleQualifiedName getQualifiedName();

	/**
	 * <p>
	 * Returns the module's version.
	 * </p>
	 * 
	 * <p>
	 * In case of a binary module, it must correspond to the version of the compiler
	 * that were used to generate the module.
	 * </p>
	 * 
	 * <p>
	 * In case of a compiled module, it must correspond to the version of the
	 * current compiler.
	 * </p>
	 * 
	 * @return A version
	 */
	int getVersion();

	/**
	 * <p>
	 * Returns the modules required by the module.
	 * </p>
	 * 
	 * @return an array of module info
	 */
	ModuleInfo[] getModules();

	/**
	 * <p>
	 * Returns the socket beans specified in the module.
	 * </p>
	 * 
	 * <p>
	 * A socket bean defines an entry point for dependency injection on the module.
	 * </p>
	 * 
	 * @return an array of socket beans
	 */
	SocketBeanInfo[] getSockets();

	/**
	 * <p>
	 * Returns an optional containing the bean identified by the specified name if
	 * it exists in the module.
	 * </p>
	 * 
	 * @param name the name of the bean in the module to return
	 * 
	 * @return an optional
	 */
	Optional<ModuleBeanInfo> getBean(String name);

	/**
	 * <p>
	 * Returns the beans specified in the module.
	 * </p>
	 * 
	 * @return an array of beans
	 */
	ModuleBeanInfo[] getBeans();

	/**
	 * <p>
	 * Returns the private beans specified in the module.
	 * </p>
	 * 
	 * @return an array of beans
	 */
	ModuleBeanInfo[] getPrivateBeans();

	/**
	 * <p>
	 * Returns the public beans specified in the module.
	 * </p>
	 * 
	 * @return an array of beans
	 */
	ModuleBeanInfo[] getPublicBeans();

	/**
	 * <p>
	 * Accepts the specified module info visitor.
	 * </p>
	 * 
	 * @param <R>     the type of the visitor result
	 * @param <P>     the type of the visitor parameter
	 * @param visitor the visitor to invoke
	 * @param p       the parameter
	 * 
	 * @return the visitor result
	 */
	<R, P> R accept(ModuleInfoVisitor<R, P> visitor, P p);

	/**
	 * <p>
	 * Determines whether the module info is faulty.
	 * </p>
	 * 
	 * <p>
	 * A module can be faulty for several reasons: name conflicts, cycles detected
	 * in the dependency graph, unresolved dependencies...
	 * </p>
	 * 
	 * @return true if the module is faulty, false otherwise
	 */
	boolean isFaulty();
}
