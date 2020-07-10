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
import java.util.logging.Level;

import io.winterframework.core.v1.Module.Bean;
import io.winterframework.core.v1.Module.BeanBuilder;
import io.winterframework.core.v1.Module.WrapperBeanBuilder;

/**
 * <p>
 * Prototype {@link BeanBuilder} implementation.
 * </p>
 * 
 * <p>
 * A {@link PrototypeWrapperBeanBuilder} must be used to create prototype beans
 * using a wrapper, when distinct bean instances must be injected into all
 * dependent beans through the application.
 * </p>
 * 
 * @author jkuhn
 * @since 1.0
 * 
 * @see BeanBuilder
 * @see Bean
 * @see PrototypeWrapperBean
 * 
 * @param <W> the type of the wrapper bean
 * @param <T> the actual type of the bean
 */
class PrototypeWrapperBeanBuilder<T, W extends Supplier<T>> extends AbstractBeanBuilder<W, WrapperBeanBuilder<W, T>> implements WrapperBeanBuilder<W, T> {

	/**
	 * <p>
	 * Creates a prototype bean builder with the specified bean name and
	 * constructor.
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
	 * Builds the bean.
	 * </p>
	 * 
	 * @return a prototype bean
	 */
	@Override
	public Bean<T> build() {
		return new PrototypeWrapperBean<W, T>(this.beanName) {

			@Override
			protected W createWrapper() {
				W wrapper = constructor.get();
				inits.stream().forEach(init -> {
					try {
						init.accept(wrapper);
					} 
					catch (Exception e) {
						LOGGER.log(Level.SEVERE, e, () -> "Error initializing bean " + name);
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
						LOGGER.log(Level.WARNING, e, () -> "Error destroying bean " + name);
					}
				});
			}
		};
	}
}
