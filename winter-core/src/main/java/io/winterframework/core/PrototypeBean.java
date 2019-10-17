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

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import io.winterframework.core.Module.Bean;

/**
 * <p>
 * A prototype {@link Bean} implementation.
 * </p>
 * 
 * <p>
 * A Prototype bean is instantiated each time it is requested, each dependent
 * bean receives a distinct instance.
 * </p>
 * 
 * @param <T>
 *            The actual type of the bean.
 * 
 * @author jkuhn
 * @since 1.0
 * @see Bean
 */
abstract class PrototypeBean<T> extends AbstractBean<T> {

	/**
	 * The bean logger.
	 */
	protected static final Logger LOGGER = Logger.getLogger(PrototypeBean.class.getName());

	/**
	 * The list of instances issued by the bean.
	 */
	protected List<T> instances;
	
	/**
	 * <p>
	 * Create a prototype bean with the specified name.
	 * </p>
	 * 
	 * @param name
	 *            The bean name
	 */
	public PrototypeBean(String name) {
		super(name);
	}
	
	/**
	 * <p>
	 * Create the prototype bean.
	 * </p>
	 * 
	 * <p>
	 * Since a new bean instance must be created each time the bean is requested,
	 * this method basically does nothing, instances being created in the
	 * {@link #get()} method.
	 * </p>
	 */
	public synchronized final void create() {
		if(this.instances == null) {
			LOGGER.info(() -> "Creating Prototype Bean " + (this.parent != null ? this.parent.getName() : "") + ":" + this.name);
			this.instances = new ArrayList<>();
			this.parent.recordBean(this);
		}
	}
	
	/**
	 * <p>
	 * Return a new bean instance.
	 * </p>
	 * 
	 * <p>
	 * This method delegates bean instance destruction to the
	 * {@link #createInstance()} method.
	 * </p>
	 * 
	 * @return A bean instance
	 */
	public final T get() {
		this.create();
		T instance = this.createInstance();
		this.instances.add(instance);
		return instance;
	}

	/**
	 * <p>
	 * Destroy the prototype bean and as a result all bean instances it has issued.
	 * </p>
	 * 
	 * <p>
	 * This method delegates bean instance destruction to the
	 * {@link #destroyInstance(Object)} method.
	 * </p>
	 */
	public synchronized final void destroy() {
		if(this.instances != null) {
			LOGGER.info(() -> "Destroying Prototype Bean " + (this.parent != null ? this.parent.getName() : "") + ":" + this.name);
			this.instances.forEach(instance -> this.destroyInstance(instance));
			this.instances.clear();
			this.instances = null;
		}
	}
}
