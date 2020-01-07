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
package io.winterframework.core.v1;

import io.winterframework.core.v1.Module.Bean;
import io.winterframework.core.v1.Module.BeanBuilder;

/**
 * <p>
 * Base class for {@link Bean} implementations
 * </p>
 * 
 * <p>
 * This class basically specifies {@link #createInstance()} and
 * {@link #destroyInstance(Object)} methods that respectively create and
 * destroy bean instances. These methods can then be used in {@link #create()}
 * and {@link #destroy()} methods which provide higher logic (singleton,
 * prototype...).
 * </p>
 * 
 * @author jkuhn
 * @since 1.0
 * 
 * @see Bean
 * @see BeanBuilder
 */
abstract class AbstractBean<T> extends Bean<T> {

	/**
	 * <p>
	 * Create an abstract bean with the specified name.
	 * </p>
	 * 
	 * @param name
	 *            The bean name
	 */
	public AbstractBean(String name) {
		super(name);
	}

	/**
	 * <p>
	 * Create a bean instance.
	 * </p>
	 * 
	 * @return A bean instance
	 */
	protected abstract T createInstance();

	/**
	 * <p>
	 * Destroy the specified bean instance.
	 * </p>
	 * 
	 * @param instance
	 *            The instance to destroy
	 */
	protected abstract void destroyInstance(T instance);
}
