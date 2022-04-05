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
import io.inverno.core.v1.Module.BeanBuilder;
import java.util.Optional;
import java.util.function.Supplier;

/**
 * <p>
 * Prototype {@link BeanBuilder} implementation.
 * </p>
 *
 * <p>
 * A {@link PrototypeWrapperBeanBuilder} must be used to create prototype beans using a wrapper, when distinct bean instances must be injected into all dependent beans through the application.
 * </p>
 *
 * @author <a href="mailto:jeremy.kuhn@inverno.io">Jeremy Kuhn</a>
 * @since 1.0
 *
 * @see BeanBuilder
 * @see Bean
 * @see PrototypeWrapperBean
 *
 * @param <P> the type provided by the bean
 * @param <W> the type of the wrapper bean
 * @param <T> the actual type of the bean
 */
class PrototypeWrapperBeanBuilder<P, T, W extends Supplier<T>> extends AbstractWrapperBeanBuilder<P, T, W> {

	/**
	 * <p>
	 * Creates a prototype wrapper bean builder with the specified bean name and constructor.
	 * </p>
	 *
	 * @param beanName    the bean name
	 * @param constructor the bean constructor
	 */
	public PrototypeWrapperBeanBuilder(String beanName, Supplier<W> constructor) {
		super(beanName, constructor);
	}

	/**
	 * <p>
	 * Creates an overridable prototype wrapper bean builder.
	 * </p>
	 *
	 * @param overriddenBuilder the overridden prototype wrapper bean builder
	 * @param override          the override
	 */
	public PrototypeWrapperBeanBuilder(PrototypeWrapperBeanBuilder<?, T, W> overriddenBuilder, Optional<Supplier<P>> override) {
		super(overriddenBuilder, override);
	}

	@Override
	public <P> Module.WrapperBeanBuilder<P, T, W> override(Optional<Supplier<P>> override) {
		return new PrototypeWrapperBeanBuilder<>(this, override);
	}
	
	/**
	 * <p>
	 * Builds the bean.
	 * </p>
	 *
	 * @return a prototype bean
	 */
	@Override
	public Bean<P> build() {
		if(this.destroys == null || this.destroys.isEmpty()) {
			return new PrototypeWrapperBean<ProvidingWrapper, P>(this.beanName, this.override) {

				@Override
				protected ProvidingWrapper createWrapper() {
					W wrapper = constructor.get();
					if(inits != null) {
						inits.stream().forEach(init -> {
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
			};
		}
		else {
			return new PrototypeWeakWrapperBean<ProvidingWrapper, P>(this.beanName, this.override) {

				@Override
				protected ProvidingWrapper createWrapper() {
					W wrapper = constructor.get();
					if(inits != null) {
						inits.stream().forEach(init -> {
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
						destroys.stream().forEach(destroy -> {
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
}
