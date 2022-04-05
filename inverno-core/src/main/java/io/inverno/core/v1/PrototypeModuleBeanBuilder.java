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
package io.inverno.core.v1;

import io.inverno.core.v1.Module.Bean;
import io.inverno.core.v1.Module.BeanBuilder;
import java.util.Optional;
import java.util.function.Supplier;

/**
 * <p>
 * Prototype module {@link BeanBuilder} implementation.
 * </p>
 *
 * <p>
 * A {@link PrototypeModuleBeanBuilder} must be used to create prototype beans, when distinct bean instances must be injected into all dependent beans through the application.
 * </p>
 *
 * @author <a href="mailto:jeremy.kuhn@inverno.io">Jeremy Kuhn</a>
 * @since 1.0
 *
 * @see BeanBuilder
 * @see Bean
 * @see PrototypeModuleBean
 *
 * @param <P> the type provided by the bean.
 * @param <T> the actual type of the bean.
 */
class PrototypeModuleBeanBuilder<P, T> extends AbstractModuleBeanBuilder<P, T> {
	
	/**
	 * <p>
	 * Creates a prototype module bean builder with the specified bean name and constructor.
	 * </p>
	 *
	 * @param beanName    the bean name
	 * @param constructor the bean constructor
	 */
	public PrototypeModuleBeanBuilder(String beanName, Supplier<T> constructor) {
		super(beanName, constructor);
	}

	/**
	 * <p>
	 * Creates an overridable prototype module bean builder.
	 * </p>
	 *
	 * @param overridenBuilder the overridden prototype module bean builder
	 * @param override         the override
	 */
	public PrototypeModuleBeanBuilder(PrototypeModuleBeanBuilder<?, T> overridenBuilder, Optional<Supplier<P>> override) {
		super(overridenBuilder, override);
	}

	@Override
	public <P> Module.ModuleBeanBuilder<P, T> override(Optional<Supplier<P>> override) {
		return new PrototypeModuleBeanBuilder<>(this, override);
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
			return new PrototypeModuleBean<P>(this.beanName, this.override) {

				@Override
				@SuppressWarnings("unchecked")
				protected P createInstance() {
					T instance = constructor.get();
					if(inits != null) {
						inits.stream().forEach(init -> {
							try {
								init.accept(instance);
							} catch (Exception e) {
								LOGGER.fatal(() -> "Error initializing bean " + name, e);
								throw new RuntimeException("Error initializing bean " + name, e);
							}
						});
					}
					return (P)instance;
				}
			};
		}
		else {
			return new PrototypeWeakModuleBean<P>(this.beanName, this.override) {

				@Override
				@SuppressWarnings("unchecked")
				protected P createInstance() {
					T instance = constructor.get();
					if(inits != null) {
						inits.stream().forEach(init -> {
							try {
								init.accept(instance);
							} catch (Exception e) {
								LOGGER.fatal(() -> "Error initializing bean " + name, e);
								throw new RuntimeException("Error initializing bean " + name, e);
							}
						});
					}
					return (P)instance;
				}

				@Override
				@SuppressWarnings("unchecked")
				protected void destroyInstance(P instance) {
					if(destroys != null) {
						destroys.stream().forEach(destroy -> {
							try {
								destroy.accept((T)instance);
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
