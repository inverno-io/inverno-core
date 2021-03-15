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
package io.winterframework.core.v1;

import java.util.Optional;
import java.util.function.Supplier;

import io.winterframework.core.v1.Module.Bean;
import io.winterframework.core.v1.Module.ModuleBeanBuilder;

/**
 * <p>
 * Base class for {@link ModuleBeanBuilder} implementations.
 * </p>
 * 
 * @author <a href="mailto:jeremy.kuhn@winterframework.io">Jeremy Kuhn</a>
 * @since 1.1
 * 
 * @see Bean
 * @see ModuleBeanBuilder
 * 
 * @param <T> the actual type of the bean built by this builder
 */
abstract class AbstractModuleBeanBuilder<T> extends AbstractBeanBuilder<T, ModuleBeanBuilder<T>> implements ModuleBeanBuilder<T> {

	/**
	 * The override that, when present, provides bean instances instead of the builder. 
	 */
	protected Optional<Supplier<T>> override = Optional.empty();
	
	/**
	 * <p>
	 * Creates a module bean builder with the specified bean name and constructor.
	 * </p>
	 * 
	 * @param beanName    the bean name
	 * @param constructor the bean constructor
	 */
	protected AbstractModuleBeanBuilder(String beanName, Supplier<T> constructor) {
		super(beanName, constructor);
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public ModuleBeanBuilder<T> override(Optional<Supplier<T>> override) {
		this.override = override != null ? override : Optional.empty();
		return this;
	}
}
