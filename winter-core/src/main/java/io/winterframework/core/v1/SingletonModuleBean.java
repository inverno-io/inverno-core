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

import java.util.logging.Logger;

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
	protected static final Logger LOGGER = Logger.getLogger(SingletonModuleBean.class.getName());

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
	 */
	public SingletonModuleBean(String name) {
		super(name);
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
			LOGGER.fine("Creating Singleton Bean " + (this.parent != null ? this.parent.getName() : "") + ":" + this.name);
			this.instance = this.createInstance();
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
			LOGGER.fine("Destroying Singleton Bean " + (this.parent != null ? this.parent.getName() : "") + ":"	+ this.name);
			this.destroyInstance(this.instance);
			this.instance = null;
		}
	}
}
