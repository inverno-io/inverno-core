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
 * A module info visitor is used to process a module info.
 * </p>
 * 
 * @author jkuhn
 *
 * @param <R> the visitor result type
 * @param <P> the visitor parameter type
 */
public interface ModuleInfoVisitor<R, P> {

	/**
	 * <p>
	 * Visit module info.
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
	 * Visit bean info.
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
	 * Visit module bean info.
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
	 * Visit wrapper bean info.
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
	 * Visit socket info.
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
	 * Visit single socket info.
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
	 * Visit multiple socket info.
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
	 * Visit bean socket info.
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
	 * Visit bean single socket info.
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
	 * Visit bean multiple socket info.
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
	 * Visit socket bean info.
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
	 * Visit single socket bean info.
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
	 * Visit multiple socket bean info.
	 * </p>
	 * 
	 * @param multiSocketBeanInfo the info to visit
	 * @param p                   a visitor parameter
	 * 
	 * @return a visitor result
	 */
	R visit(MultiSocketBeanInfo multiSocketBeanInfo, P p);
}
