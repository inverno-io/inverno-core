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
package io.inverno.core.compiler.spi;

/**
 * <p>
 * An overriding socket bean info is an single socket bean info used as an
 * override for an {@link OverridableBeanInfo}.
 * </p>
 * 
 * @author <a href="mailto:jeremy.kuhn@inverno.io">Jeremy Kuhn</a>
 *
 */
public interface OverridingSocketBeanInfo extends SingleSocketBeanInfo {

	/**
	 * <p>
	 * An overriding socket bean is necessarily optional.
	 * </p>
	 * 
	 * @return true
	 */
	@Override
	default boolean isOptional() {
		return true;
	}
	
	/**
	 * <p>
	 * An overriding socket bean is wired to one corresponding overridable bean. 
	 * </p>
	 * 
	 * @return true
	 */
	@Override
	default boolean isWired() {
		return true;
	}
}
