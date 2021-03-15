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

import javax.lang.model.type.TypeMirror;

/**
 * <p>
 * Base bean info interface specifying data and services common to all beans.
 * </p>
 * 
 * @author <a href="mailto:jeremy.kuhn@winterframework.io">Jeremy Kuhn</a>
 *
 */
public interface BeanInfo extends Info {

	/**
	 * <p>
	 * Returns the bean qualified name.
	 * </p>
	 * 
	 * @return a bean qualified name
	 */
	BeanQualifiedName getQualifiedName();

	/**
	 * <p>
	 * Returns the underlying type of the bean.
	 * </p>
	 * 
	 * @return a type
	 */
	TypeMirror getType();
	
	/**
	 * <p>
	 * Returns a list of beans nested in this bean, ie. provided by this bean.
	 * </p>
	 * 
	 * <p>
	 * A nested bean follows the lifecyle of its providing bean.
	 * </p>
	 * 
	 * @return an array of nested beans
	 */
	NestedBeanInfo[] getNestedBeans();
}
