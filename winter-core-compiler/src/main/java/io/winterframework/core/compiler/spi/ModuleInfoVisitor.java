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
 * @param <R> The visitor result type.
 * @param <P> The visitor parameter type.
 */
public interface ModuleInfoVisitor<R, P> {

	R visit(ModuleInfo moduleInfo, P p);
	
	R visit(BeanInfo beanInfo, P p);
	
	R visit(ModuleBeanInfo moduleBeanInfo, P p);
	
	R visit(WrapperBeanInfo moduleWrapperBeanInfo, P p);
	
	R visit(SocketInfo socketInfo, P p);
	
	R visit(SingleSocketInfo singleSocketInfo, P p);
	
	R visit(MultiSocketInfo multiSocketInfo, P p);
	
	R visit(ModuleBeanSocketInfo beanSocketInfo, P p);
	
	R visit(ModuleBeanSingleSocketInfo beanSingleSocketInfo, P p);
	
	R visit(ModuleBeanMultiSocketInfo beanMultiSocketInfo, P p);
	
	R visit(SocketBeanInfo socketBeanInfo, P p);
	
	R visit(SingleSocketBeanInfo singleSocketBeanInfo, P p);
	
	R visit(MultiSocketBeanInfo multiSocketBeanInfo, P p);
}
