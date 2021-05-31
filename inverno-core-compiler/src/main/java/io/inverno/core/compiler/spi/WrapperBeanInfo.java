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

import javax.lang.model.type.TypeMirror;

/**
 * <p>
 * A wrapper bean info holds the data required to process a wrapper bean in a
 * module.
 * </p>
 * 
 * @author <a href="mailto:jeremy.kuhn@inverno.io">Jeremy Kuhn</a>
 *
 */
public interface WrapperBeanInfo extends ModuleBeanInfo {

	/**
	 * <p>
	 * Returns the wrapper type which is the type of the class supplying the actual
	 * bean whose type is given by {@link BeanInfo#getType()}.
	 * </p>
	 * 
	 * @return a type
	 */
	TypeMirror getWrapperType();
}
