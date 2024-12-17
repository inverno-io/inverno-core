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

/**
 * <p>
 * A module info visitor is used to process a module info.
 * </p>
 * 
 * @author <a href="mailto:jeremy.kuhn@inverno.io">Jeremy Kuhn</a>
 *
 * @param <R> the visitor result type
 * @param <P> the visitor parameter type
 */
public interface ModuleInfoVisitor<R, P> {

	/**
	 * <p>
	 * Visits module info.
	 * </p>
	 * 
	 * @param moduleInfo the info to visit
	 * @param p          a visitor parameter
	 * 
	 * @return a visitor result
	 */
	R visit(ModuleInfo moduleInfo, P p);

	/**
	 * <p>
	 * Visits bean info.
	 * </p>
	 * 
	 * @param beanInfo the info to visit
	 * @param p        a visitor parameter
	 * 
	 * @return a visitor result
	 */
	R visit(BeanInfo beanInfo, P p);
	
	/**
	 * <p>
	 * Visits nested bean info.
	 * </p>
	 * 
	 * @param nestedBeanInfo the info to visit
	 * @param p              a visitor parameter
	 * 
	 * @return a visitor result
	 */
	R visit(NestedBeanInfo nestedBeanInfo, P p);

	/**
	 * <p>
	 * Visits module bean info.
	 * </p>
	 * 
	 * @param moduleBeanInfo the info to visit
	 * @param p              a visitor parameter
	 * 
	 * @return a visitor result
	 */
	R visit(ModuleBeanInfo moduleBeanInfo, P p);

	/**
	 * <p>
	 * Visits wrapper bean info.
	 * </p>
	 * 
	 * @param wrapperBeanInfo the info to visit
	 * @param p               a visitor parameter
	 * 
	 * @return a visitor result
	 */
	R visit(WrapperBeanInfo wrapperBeanInfo, P p);
	
	/**
	 * <p>
	 * Visits overridable bean info.
	 * </p>
	 * 
	 * @param overridableBeanInfo the info to visit
	 * @param p               a visitor parameter
	 * 
	 * @return a visitor result
	 */
	R visit(OverridableBeanInfo overridableBeanInfo, P p);
	
	/**
	 * <p>
	 * Visits mutator bean info.
	 * </p>
	 * 
	 * @param mutatorBeanInfo the info to visit
	 * @param p               a visitor parameter
	 * 
	 * @return a visitor result
	 */
	R visit(MutatorBeanInfo mutatorBeanInfo, P p);
	
	/**
	 * <p>
	 * Visits overriding socket bean info.
	 * </p>
	 * 
	 * @param overridingSocketBeanInfo the info to visit
	 * @param p               a visitor parameter
	 * 
	 * @return a visitor result
	 */
	R visit(OverridingSocketBeanInfo overridingSocketBeanInfo, P p);

	/**
	 * <p>
	 * Visits socket info.
	 * </p>
	 * 
	 * @param socketInfo the info to visit
	 * @param p          a visitor parameter
	 * 
	 * @return a visitor result
	 */
	R visit(SocketInfo socketInfo, P p);

	/**
	 * <p>
	 * Visits single socket info.
	 * </p>
	 * 
	 * @param singleSocketInfo the info to visit
	 * @param p                a visitor parameter
	 * 
	 * @return a visitor result
	 */
	R visit(SingleSocketInfo singleSocketInfo, P p);

	/**
	 * <p>
	 * Visits multiple socket info.
	 * </p>
	 * 
	 * @param multiSocketInfo the info to visit
	 * @param p               a visitor parameter
	 * 
	 * @return a visitor result
	 */
	R visit(MultiSocketInfo multiSocketInfo, P p);

	/**
	 * <p>
	 * Visits bean socket info.
	 * </p>
	 * 
	 * @param beanSocketInfo the info to visit
	 * @param p              a visitor parameter
	 * 
	 * @return a visitor result
	 */
	R visit(ModuleBeanSocketInfo beanSocketInfo, P p);

	/**
	 * <p>
	 * Visits bean single socket info.
	 * </p>
	 * 
	 * @param beanSingleSocketInfo the info to visit
	 * @param p                    a visitor parameter
	 * 
	 * @return a visitor result
	 */
	R visit(ModuleBeanSingleSocketInfo beanSingleSocketInfo, P p);

	/**
	 * <p>
	 * Visits bean multiple socket info.
	 * </p>
	 * 
	 * @param beanMultiSocketInfo the info to visit
	 * @param p                   a visitor parameter
	 * 
	 * @return a visitor result
	 */
	R visit(ModuleBeanMultiSocketInfo beanMultiSocketInfo, P p);

	/**
	 * <p>
	 * Visits socket bean info.
	 * </p>
	 * 
	 * @param socketBeanInfo the info to visit
	 * @param p              a visitor parameter
	 * 
	 * @return a visitor result
	 */
	R visit(SocketBeanInfo socketBeanInfo, P p);

	/**
	 * <p>
	 * Visits single socket bean info.
	 * </p>
	 * 
	 * @param singleSocketBeanInfo the info to visit
	 * @param p                    a visitor parameter
	 * 
	 * @return a visitor result
	 */
	R visit(SingleSocketBeanInfo singleSocketBeanInfo, P p);

	/**
	 * <p>
	 * Visits multiple socket bean info.
	 * </p>
	 * 
	 * @param multiSocketBeanInfo the info to visit
	 * @param p                   a visitor parameter
	 * 
	 * @return a visitor result
	 */
	R visit(MultiSocketBeanInfo multiSocketBeanInfo, P p);
}
