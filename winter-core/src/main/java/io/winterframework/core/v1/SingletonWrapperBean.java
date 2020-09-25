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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import io.winterframework.core.v1.Module.Bean;

/**
 * <p>
 * A singleton wrapper {@link Bean} implementation.
 * </p>
 * 
 * <p>
 * A Singleton bean is instantiated once for the whole application, every
 * dependent beans receive the same instance.
 * </p>
 * 
 * @author jkuhn
 * @since 1.0
 * @see Bean
 * 
 * @param <W> the type of the wrapper bean
 * @param <T> the actual type of the bean
 */
abstract class SingletonWrapperBean<W extends Supplier<T>, T> extends AbstractWrapperBean<W, T> {

	/**
	 * The bean logger.
	 */
	protected static final Logger LOGGER = LogManager.getLogger(SingletonWrapperBean.class);

	/**
	 * The wrapper instance.
	 */
	protected W wrapper;
	
	/**
	 * The bean instance.
	 */
	protected T instance;
	
	/**
	 * <p>
	 * Creates a singleton wrapper bean with the specified name.
	 * </p>
	 * 
	 * @param name the bean name
	 * @param override An optional override
	 */
	public SingletonWrapperBean(String name, Optional<Supplier<T>> override) {
		super(name, override);
	}

	/**
	 * <p>
	 * Creates the singleton bean.
	 * </p>
	 * 
	 * <p>
	 * This method delegates bean instantiation to the wrapper instance returned by
	 * {@link #createWrapper()} method and implement the singleton pattern.
	 * </p>
	 */
	public synchronized final void create() {
		if (this.wrapper == null) {
			LOGGER.debug("Creating singleton bean {} ({})", () -> (this.parent != null ? this.parent.getName() + ":" : "") + this.name, () -> this.override.map(s -> "overridden").orElse(""));
			this.instance = this.override.map(Supplier::get).orElseGet(() -> {
				this.wrapper = this.createWrapper();
				return this.wrapper.get();
			});
			this.parent.recordBean(this);
		}
	}

	/**
	 * <p>
	 * Returns the bean singleton.
	 * </p>
	 * 
	 * @return the bean singleton
	 */
	public final T doGet() {
		this.create();
		return this.instance;
	}

	/**
	 * <p>
	 * Destroys the singleton bean and as a result the enclosed wrapper instance
	 * which eventually destroy the bean instance.
	 * </p>
	 * 
	 * <p>
	 * This method delegates bean wrapper instance destruction to the
	 * {@link #destroyWrapper(Object)} method.
	 * </p>
	 */
	public synchronized final void destroy() {
		if (this.wrapper != null) {
			LOGGER.debug("Destroying singleton bean {}", () -> (this.parent != null ? this.parent.getName() + ":" : "") + this.name);
			if(!this.override.isPresent()) {
				this.destroyWrapper(this.wrapper);
				this.wrapper = null;
			}
			this.instance = null;
		}
	}
}
