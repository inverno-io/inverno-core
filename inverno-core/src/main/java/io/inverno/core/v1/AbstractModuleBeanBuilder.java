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
import io.inverno.core.v1.Module.ModuleBeanBuilder;
import java.util.LinkedList;
import java.util.Optional;
import java.util.function.Supplier;

/**
 * <p>
 * Base class for {@link ModuleBeanBuilder} implementations.
 * </p>
 * 
 * @author <a href="mailto:jeremy.kuhn@inverno.io">Jeremy Kuhn</a>
 * @since 1.1
 * 
 * @see Bean
 * @see ModuleBeanBuilder
 * 
 * @param <P> the type provided by the bean to build
 * @param <T> the actual type of the bean to build
 */
abstract class AbstractModuleBeanBuilder<P, T> extends AbstractBeanBuilder<T, ModuleBeanBuilder<P, T>> implements ModuleBeanBuilder<P, T> {

	/**
	 * The override that, when present, provides bean instances instead of the builder. 
	 */
	protected final Optional<Supplier<P>> override;
	
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
		this.override = Optional.empty();
	}
	
	/**
	 * <p>
	 * Creates an overridable module bean builder.
	 * </p>
	 * 
	 * @param overriddenBuilder the overridden module bean builder
	 * @param override          the override
	 */
	protected AbstractModuleBeanBuilder(AbstractModuleBeanBuilder<?, T> overriddenBuilder, Optional<Supplier<P>> override) {
		super(overriddenBuilder.beanName, overriddenBuilder.constructor);
		this.override = override != null ? override : Optional.empty();
		this.inits = overriddenBuilder.inits != null ? new LinkedList<>(overriddenBuilder.inits) : null;
		this.destroys = overriddenBuilder.destroys != null ? new LinkedList<>(overriddenBuilder.destroys) : null;
	}
}
