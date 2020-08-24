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

import java.util.function.Supplier;

import io.winterframework.core.v1.Module.Bean;
import io.winterframework.core.v1.Module.BeanBuilder;
import io.winterframework.core.v1.Module.WrapperBeanBuilder;

/**
 * <p>
 * Singleton wrapper {@link BeanBuilder} implementation.
 * </p>
 * 
 * <p>
 * A {@link SingletonWrapperBeanBuilder} must be used to create singleton beans
 * using a wrapper, when the same bean instance must be injected into all
 * dependent beans through the application.
 * </p>
 * 
 * @author jkuhn
 * @since 1.0
 * 
 * @see BeanBuilder
 * @see Bean
 * @see SingletonWrapperBean
 * 
 * @param <W> the type of the wrapper bean
 * @param <T> the actual type of the bean
 */
class SingletonWrapperBeanBuilder<W extends Supplier<T>, T> extends AbstractBeanBuilder<W, WrapperBeanBuilder<W, T>> implements WrapperBeanBuilder<W, T> {

	/**
	 * <p>
	 * Creates a singleton wrapper bean builder with the specified bean name and
	 * constructor.
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
	 * Builds the bean.
	 * </p>
	 * 
	 * @return a singleton bean
	 */
	@Override
	public Bean<T> build() {
		return new SingletonWrapperBean<W, T>(this.beanName) {

			@Override
			protected W createWrapper() {
				W wrapper =	constructor.get();
				inits.stream().forEach(init -> {
					try {
						init.accept(wrapper);
					} 
					catch (Exception e) {
						LOGGER.fatal(() -> "Error initializing bean " + name, e);
						throw new RuntimeException("Error initializing bean " + name, e);
					}
				});
				return wrapper;
			}

			@Override
			protected void destroyWrapper(W wrapper) {
				destroys.stream().forEach(destroy -> {
					try {
						destroy.accept(wrapper);
					} catch (Exception e) {
						LOGGER.warn(() -> "Error destroying bean " + name, e);
					}
				});
			}
		};
	}
}
