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

import java.util.function.Supplier;

import io.winterframework.core.v1.Module.Bean;
import io.winterframework.core.v1.Module.BeanBuilder;

/**
 * <p>
 * Prototype {@link BeanBuilder} implementation.
 * </p>
 * 
 * <p>
 * A {@link PrototypeBeanBuilder} must be used to create prototype beans, when
 * distinct bean instances must be injected into all dependent beans through the
 * application.
 * </p>
 * 
 * @author jkuhn
 * @since 1.0
 * 
 * @param <T>
 *            The actual type of the bean.
 * 
 * @see BeanBuilder
 * @see Bean
 * @see SingletonBean
 */
class PrototypeBeanBuilder<T> extends AbstractBeanBuilder<T> {

	/**
	 * <p>
	 * Create a prototype bean builder with the specified bean name and constructor.
	 * </p>
	 * 
	 * @param beanName
	 *            The bean name
	 * @param constructor
	 *            The bean constructor
	 */
	public PrototypeBeanBuilder(String beanName, Supplier<T> constructor) {
		super(beanName, constructor);
	}

	/**
	 * <p>
	 * Build the bean.
	 * </p>
	 * 
	 * @return A prototype bean
	 */
	public Bean<T> build() {
		return new PrototypeBean<T>(this.beanName) {

			@Override
			protected T createInstance() {
				T instance = constructor.get();
				inits.stream().forEach(init -> init.accept(instance));
				return instance;
			}

			@Override
			protected void destroyInstance(T instance) {
				destroys.stream().forEach(destroy -> destroy.accept(instance));
			}
		};
	}
}
