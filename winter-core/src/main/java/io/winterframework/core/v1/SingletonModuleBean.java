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

import java.util.Optional;
import java.util.function.Supplier;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import io.winterframework.core.v1.Module.Bean;

/**
 * <p>
 * A singleton module {@link Bean} implementation.
 * </p>
 * 
 * <p>
 * A Singleton module bean is instantiated once for the whole application, every
 * dependent beans receive the same instance.
 * </p>
 * 
 * @author jkuhn
 * @since 1.0
 * @see Bean
 * 
 * @param <T> the actual type of the bean
 */
abstract class SingletonModuleBean<T> extends AbstractModuleBean<T> {

	/**
	 * The bean logger.
	 */
	protected static final Logger LOGGER = LogManager.getLogger(SingletonModuleBean.class);

	/**
	 * The bean instance.
	 */
	protected T instance;
	
	/**
	 * <p>
	 * Creates a singleton module bean with the specified name.
	 * </p>
	 * 
	 * @param name the bean name
	 * @param override An optional override
	 */
	public SingletonModuleBean(String name, Optional<Supplier<T>> override) {
		super(name, override);
	}

	/**
	 * <p>
	 * Creates the singleton bean.
	 * </p>
	 * 
	 * <p>
	 * This method delegates bean instantiation to the {@link #createInstance()}
	 * method and implement the singleton pattern.
	 * </p>
	 */
	public synchronized final void create() {
		if (this.instance == null) {
			LOGGER.debug("Creating singleton bean {} {}", () -> (this.parent != null ? this.parent.getName() + ":" : "") + this.name, () -> this.override.map(s -> "(overridden)").orElse(""));
			this.instance = this.override.map(Supplier::get).orElseGet(this::createInstance);
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
	 * Destroys the singleton bean and as a result the enclosed instance.
	 * </p>
	 * 
	 * <p>
	 * This method delegates bean instance destruction to the
	 * {@link #destroyInstance(Object)} method.
	 * </p>
	 */
	public synchronized final void destroy() {
		if (this.instance != null) {
			LOGGER.debug("Destroying singleton bean {}", () -> (this.parent != null ? this.parent.getName() + ":" : "") + this.name);
			if(!this.override.isPresent()) {
				this.destroyInstance(this.instance);
			}
			this.instance = null;
		}
	}
}
