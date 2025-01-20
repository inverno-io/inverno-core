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
package io.inverno.core.v1;

import io.inverno.core.v1.Module.Bean;
import java.lang.ref.WeakReference;
import java.util.Optional;
import java.util.function.Supplier;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * <p>
 * A prototype wrapper {@link Bean} implementation.
 * </p>
 *
 * <p>
 * A Prototype bean is instantiated each time it is requested, a distinct instance is injected into each dependent bean.
 * </p>
 *
 * <p>
 * Unlike {@link PrototypeWeakWrapperBean}, this implementation doesn't keep track of the instances it creates which makes it faster and consumes less resources, in return instances must always be
 * destroyed explicitly. This implementation should be used for beans that do not define any destroy method.
 * </p>
 *
 * <p>
 * The actual bean instance of a wrapper bean is provided by a wrapper instance to which instantiation, initialization and destruction operations are delegated. To each bean instance corresponds a
 * wrapper instance. There is no requirement that a new or distinct result be returned each time the wrapper is invoked, however when specifying initialization or destruction operations, the wrapper,
 * as indicated by its name, will usually wrap a single instance so that destruction operations can be invoked at later stage when the bean is destroyed. In that case, particular care must be taken to
 * make sure the wrapper instance does not hold a strong reference to the actual instance, otherwise bean instances created outside the module might not be reclaimed by the garbage collector leading
 * to memory leaks. A {@link WeakReference} should be then used in such situations. Note that this issue does not exist for singleton wrapper beans.
 * </p>
 *
 * @author <a href="mailto:jeremy.kuhn@inverno.io">Jeremy Kuhn</a>
 * @since 1.0
 * @see Bean
 *
 * @param <W> the type of the wrapper bean
 * @param <T> the actual type of the bean
 */
abstract class PrototypeWrapperBean<W extends Supplier<T>, T> extends AbstractWrapperBean<W, T> {

	/**
	 * The bean logger.
	 */
	protected static final Logger LOGGER = LogManager.getLogger(PrototypeWrapperBean.class);
	
	private boolean created;
	
	/**
	 * <p>
	 * Creates a prototype wrapper bean with the specified name.
	 * </p>
	 *
	 * @param name     the bean name
	 * @param override An optional override
	 */
	public PrototypeWrapperBean(String name, Optional<Supplier<T>> override) {
		super(name, override);
	}
	
	/**
	 * <p>
	 * Creates the prototype bean.
	 * </p>
	 *
	 * <p>
	 * Since a new bean instance must be created each time the bean is requested, this method basically does nothing, instances being created in the {@link #get()} method.
	 * </p>
	 */
	@Override
	public final void create() {
		if (!this.created) {
			synchronized(this) {
				LOGGER.debug("Creating prototype bean {} {}", () -> (this.parent != null ? this.parent.getName() + ":" : "") + this.name, () -> this.override.map(s -> "(overridden)").orElse(""));
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
	 * This method delegates bean instance creation to a wrapper instance returned by {@link #createWrapper()} method.
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
				W wrapper = this.createWrapper();
				return wrapper.get();
			});
	}

	/**
	 * <p>
	 * Destroys the prototype bean.
	 * </p>
	 *
	 * <p>
	 * Since no references to the instances created by this bean have been kept, this method basically does nothing.
	 * </p>
	 */
	@Override
	public final void destroy() {
		synchronized(this) {
			LOGGER.debug("Destroying prototype bean {}", () -> (this.parent != null ? this.parent.getName() + ":" : "") + this.name);
		}
	}
	
	@Override
	protected void destroyWrapper(W wrapper) {
		
	}
}
