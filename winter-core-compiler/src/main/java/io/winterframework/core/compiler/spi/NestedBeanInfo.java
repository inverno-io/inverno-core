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

import javax.lang.model.element.ExecutableElement;

/**
 * <p>
 * A nested bean info holds the data required to process a nested bean in a
 * module.
 * </p>
 * 
 * <p>
 * Nested beans are exposed and provided by a bean (module or socket bean) in a
 * module. They follow the same lifecycle as their providing bean as such they
 * are not directly exposed on a module.
 * </p>
 * 
 * @author jkuhn
 *
 */
public interface NestedBeanInfo extends BeanInfo {

	ExecutableElement getAccessorElement();
	
	/**
	 * <p>
	 * Returns the name of the nested bean in the providing bean.
	 * </p>
	 * 
	 * @return A name
	 */
	String getName();

	/**
	 * <p>
	 * Returns the bean providing the nested bean.
	 * </p>
	 * 
	 * @return A bean
	 */
	BeanInfo getProvidingBean();
}
