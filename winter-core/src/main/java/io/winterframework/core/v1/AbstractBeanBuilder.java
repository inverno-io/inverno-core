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

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import io.winterframework.core.v1.Module.Bean;
import io.winterframework.core.v1.Module.BeanBuilder;

/**
 * <p>
 * Base class for {@link BeanBuilder} implementations.
 * </p>
 * 
 * @author <a href="mailto:jeremy.kuhn@winterframework.io">Jeremy Kuhn</a>
 * @since 1.0
 * 
 * @see Bean
 * @see BeanBuilder
 * 
 * @param <T> the actual type of the bean built by this builder
 * @param <B> the bean builder type to support method chaining
 */
abstract class AbstractBeanBuilder<T, B extends BeanBuilder<T,B>> implements BeanBuilder<T, B> {

	/**
	 * The bean name.
	 */
	protected String beanName;

	/**
	 * The bean constructor.
	 */
	protected Supplier<T> constructor;

	/**
	 * The list of bean initialization operations that must be executed after bean
	 * instance creation and dependency injection.
	 */
	protected List<FallibleConsumer<T>> inits;

	/**
	 * The list of bean destructions operations that must be executed after a bean
	 * instance creation and dependency injection.
	 */
	protected List<FallibleConsumer<T>> destroys;
	
	/**
	 * <p>
	 * Creates a bean builder with the specified bean name and constructor.
	 * </p>
	 * 
	 * @param beanName    the bean name
	 * @param constructor the bean constructor
	 */
	protected AbstractBeanBuilder(String beanName, Supplier<T> constructor) {
		this.beanName = beanName;
		this.constructor = constructor;

		this.inits = new ArrayList<>();
		this.destroys = new ArrayList<>();
	}

	/**
	 * {@inheritDoc}
	 */
	@SuppressWarnings("unchecked")
	@Override
	public B init(FallibleConsumer<T> init) {
		this.inits.add(init);
		return (B)this;
	}

	/**
	 * {@inheritDoc}
	 */
	@SuppressWarnings("unchecked")
	@Override
	public B destroy(FallibleConsumer<T> destroy) {
		this.destroys.add(destroy);
		return (B)this;
	}
}
