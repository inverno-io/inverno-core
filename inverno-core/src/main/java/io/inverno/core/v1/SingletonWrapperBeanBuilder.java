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

import java.util.Optional;
import java.util.function.Supplier;

import io.inverno.core.v1.Module.Bean;
import io.inverno.core.v1.Module.BeanBuilder;

/**
 * <p>
 * Singleton wrapper {@link BeanBuilder} implementation.
 * </p>
 *
 * <p>
 * A {@link SingletonWrapperBeanBuilder} must be used to create singleton beans using a wrapper, when the same bean instance must be injected into all dependent beans through the application.
 * </p>
 *
 * @author <a href="mailto:jeremy.kuhn@inverno.io">Jeremy Kuhn</a>
 * @since 1.0
 *
 * @see BeanBuilder
 * @see Bean
 * @see SingletonWrapperBean
 *
 * @param <P> the type provided by the bean
 * @param <W> the type of the wrapper bean
 * @param <T> the actual type of the bean
 */
class SingletonWrapperBeanBuilder<P, T, W extends Supplier<T>> extends AbstractWrapperBeanBuilder<P, T, W> {

	/**
	 * <p>
	 * Creates a singleton wrapper bean builder with the specified bean name and constructor.
	 * </p>
	 *
	 * @param beanName    the bean name
	 * @param constructor the bean constructor
	 */
	public SingletonWrapperBeanBuilder(String beanName, Supplier<W> constructor) {
		super(beanName, constructor);
	}
	
	/**
	 * <p>
	 * Creates an overridable singleton wrapper bean builder.
	 * </p>
	 *
	 * @param overriddenBuilder the overridden singleton wrapper bean builder
	 * @param override          the override
	 */
	public SingletonWrapperBeanBuilder(SingletonWrapperBeanBuilder<?, T, W> overriddenBuilder, Optional<Supplier<P>> override) {
		super(overriddenBuilder, override);
	}

	@Override
	public <U> Module.WrapperBeanBuilder<U, T, W> override(Optional<Supplier<U>> override) {
		return new SingletonWrapperBeanBuilder<>(this, override);
	}
	
	/**
	 * <p>
	 * Builds the bean.
	 * </p>
	 * 
	 * @return a singleton bean
	 */
	@Override
	public Bean<P> build() {
		return new SingletonWrapperBean<ProvidingWrapper, P>(this.beanName, this.override) {

			@Override
			protected ProvidingWrapper createWrapper() {
				W wrapper =	constructor.get();
				if(inits != null) {
					inits.forEach(init -> {
						try {
							init.accept(wrapper);
						} 
						catch (Exception e) {
							LOGGER.fatal(() -> "Error initializing bean " + name, e);
							throw new RuntimeException("Error initializing bean " + name, e);
						}
					});
				}
				return new ProvidingWrapper(wrapper);
			}

			@Override
			protected void destroyWrapper(ProvidingWrapper wrapper) {
				if(destroys != null) {
					destroys.forEach(destroy -> {
						try {
							destroy.accept(wrapper.wrapper);
						} catch (Exception e) {
							LOGGER.warn(() -> "Error destroying bean " + name, e);
						}
					});
				}
			}
		};
	}
}
