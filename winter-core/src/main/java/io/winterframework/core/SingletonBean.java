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
package io.winterframework.core;

import java.util.logging.Logger;

import io.winterframework.core.Module.Bean;

/**
 * <p>
 * A singleton {@link Bean} implementation.
 * </p>
 * 
 * <p>
 * A Singleton bean is instantiated once for the whole application, every
 * dependent beans receive the same instance.
 * </p>
 * 
 * @param <T>
 *            The actual type of the bean.
 * 
 * @author jkuhn
 * @since 1.0
 * @see Bean
 */
abstract class SingletonBean<T> extends AbstractBean<T> {

	/**
	 * The bean logger.
	 */
	protected static final Logger LOGGER = Logger.getLogger(SingletonBean.class.getName());
	
	/**
	 * The bean instance.
	 */
	protected T instance;
	
	/**
	 * <p>
	 * Create a singleton bean with the specified name.
	 * </p>
	 * 
	 * @param name
	 *            The bean name
	 */
	public SingletonBean(String name) {
		super(name);
	}

	/**
	 * <p>
	 * Create the singleton bean.
	 * </p>
	 * 
	 * <p>
	 * This method delegates bean instantiation to the {@link #createInstance()}
	 * method and implement the singleton pattern.
	 * </p>
	 */
	public synchronized  final void create() {
		if(this.instance == null) {
			LOGGER.info("Creating Singleton Bean " + (this.parent != null ? this.parent.getName() : "") + ":" + this.name);
			this.instance = this.createInstance();
		}
	}
	
	/**
	 * <p>
	 * Return the bean singleton.
	 * </p>
	 * 
	 * @return The bean singleton
	 */
	public final T get() {
		this.create();
		return this.instance;
	}
	
	/**
	 * <p>
	 * Destroy the singleton bean and as a result the enclosed instance.
	 * </p>
	 * 
	 * <p>
	 * This method delegates bean instance destruction to the
	 * {@link #destroyInstance(Object)} method.
	 * </p>
	 */
	public synchronized final void destroy() {
		if(this.instance != null) {
			LOGGER.info("Destroying Singleton Bean " + (this.parent != null ? this.parent.getName() : "")  + ":" + this.name);
			this.destroyInstance(this.instance);
			this.instance = null;
		}
	}
}
