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
package io.inverno.core.v1;

import io.inverno.core.v1.Module.Bean;
import io.inverno.core.v1.Module.WrapperBeanBuilder;
import java.util.Optional;
import java.util.function.Supplier;

/**
 * <p>
 * Base class for {@link Bean} implementations designating wrapper beans.
 * </p>
 *
 * <p>
 * This class basically specifies {@link #createWrapper()} and {@link #destroyWrapper(Object)} methods that respectively create and destroy bean wrappers instances which wraps the actual instance
 * creation logic. These methods can then be used in {@link #create()}, {@link #doGet()} and {@link #destroy()} methods which provide higher logic (singleton, prototype...).
 * </p>
 *
 * @author <a href="mailto:jeremy.kuhn@inverno.io">Jeremy Kuhn</a>
 * @since 1.0
 *
 * @see Bean
 * @see WrapperBeanBuilder
 *
 * @param <W> the type of the wrapper bean
 * @param <T> the actual type of the bean
 */
abstract class AbstractWrapperBean<W extends Supplier<T>, T> extends Bean<T> {

	protected final Optional<Supplier<T>> override;
	
	/**
	 * <p>
	 * Creates an abstract wrapper bean with the specified name.
	 * </p>
	 * 
	 * @param name the bean name
	 */
	public AbstractWrapperBean(String name, Optional<Supplier<T>> override) {
		super(name);
		this.override = override;
	}

	/**
	 * <p>
	 * Creates a wrapper instance.
	 * </p>
	 * 
	 * @return a wrapper instance
	 */
	protected abstract W createWrapper();

	/**
	 * <p>
	 * Destroys the specified wrapper instance.
	 * </p>
	 * 
	 * @param wrapper the wrapper instance to destroy
	 */
	protected abstract void destroyWrapper(W wrapper);
}
