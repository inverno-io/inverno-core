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

import java.util.Optional;
import java.util.function.Supplier;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import io.inverno.core.v1.Module.Bean;

/**
 * <p>
 * A prototype module {@link Bean} implementation.
 * </p>
 * 
 * <p>
 * A Prototype bean is instantiated each time it is requested, a distinct
 * instance is injected into each dependent bean.
 * </p>
 * 
 * <p>
 * Unlike {@link PrototypeWeakModuleBean}, this implementation doesn't keep
 * track of the instances it creates which makes it faster and consumes less
 * resources, in return instances must always be destroyed explicitly. This
 * implementation should be used for beans that do not define any destroy
 * method.
 * </p>
 * 
 * @author <a href="mailto:jeremy.kuhn@inverno.io">Jeremy Kuhn</a>
 * @since 1.0
 * @see Bean
 * @see PrototypeWeakModuleBean
 * @see PrototypeModuleBeanBuilder
 * 
 * @param <T> the actual type of the bean
 */
abstract class PrototypeModuleBean<T> extends AbstractModuleBean<T> {

	/**
	 * The bean logger.
	 */
	protected static final Logger LOGGER = LogManager.getLogger(PrototypeModuleBean.class);

	private boolean created;
	
	/**
	 * <p>
	 * Creates a prototype module bean with the specified name.
	 * </p>
	 * 
	 * @param name the bean name
	 * @param override An optional override
	 */
	public PrototypeModuleBean(String name, Optional<Supplier<T>> override) {
		super(name, override);
	}

	/**
	 * <p>
	 * Creates the prototype bean.
	 * </p>
	 * 
	 * <p>
	 * Since a new bean instance must be created each time the bean is requested,
	 * this method basically does nothing, instances being created in the
	 * {@link #get()} method.
	 * </p>
	 */
	@Override
	public final void create() {
		if (!this.created) {
			synchronized(this) {
				LOGGER.debug("Creating prototype bean {} {}", () ->  (this.parent != null ? this.parent.getName() + ":" : "") + this.name, () -> this.override.map(s -> "(overridden)").orElse(""));
				this.parent.recordBean(this);
				this.created = true;
			}
		}
	}

	/**
	 * <p>
	 * Returns a new bean instance.
	 * </p>
	 * 
	 * <p>
	 * This method delegates bean instance creation to the {@link #createInstance()}
	 * method.
	 * </p>
	 * 
	 * @return a bean instance
	 */
	@Override
	public final T doGet() {
		this.create();
		return this.override
			.map(Supplier::get)
			.orElseGet(() -> {
				return this.createInstance();
			});
	}

	/**
	 * <p>
	 * Destroys the prototype bean.
	 * </p>
	 * 
	 * <p>
	 * Since no references to the instances created by this bean have been kept,
	 * this method basically does nothing.
	 * </p>
	 */
	@Override
	public final void destroy() {
		synchronized(this) {
			LOGGER.debug("Destroying prototype bean {}", () ->  (this.parent != null ? this.parent.getName() + ":" : "") + this.name);
		}
	}
	
	@Override
	protected void destroyInstance(T instance) {
		
	}
}
