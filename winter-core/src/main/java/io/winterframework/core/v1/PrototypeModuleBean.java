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

import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.logging.Logger;

import io.winterframework.core.v1.Module.Bean;

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
 * Particular care must be taken when creating prototype beans instances outside
 * of a module (eg. moduleInstance.prototypeBean()). Modules keep weak
 * references on the prototype beans instances it creates to be able to properly
 * destroy them. The use of weak references prevent memory leaks. This works
 * properly for prototype beans instances injected into singleton instances, but
 * it is not possible to do so with a prototype bean instance referenced outside
 * of a module as it is not possible to access the instance once it has been
 * dereferenced and processed by the garbage collector. When a module is stopped
 * the behavior is then unpredictable and depends on whether the bean instance
 * is still referenced or the garbage collector has yet enqueued its reference.
 * To sum up when a module is stopped, prototype beans instances referenced in
 * singleton beans instances or referenced outside the module are always
 * destroyed and they might be destroyed if they have been, but are no longer,
 * referenced outside the module.
 * </p>
 * 
 * <p>
 * If you want to create disposable beans that live outside a module, you should
 * consider creating prototype beans that implement {@link AutoCloseable},
 * define the <code>close()</code> as destroy method, make sure it can be
 * invoked twice because it might, and get new instances as follows:
 * </p>
 * 
 * <pre>
 *     try (MyPrototype instance = myModuleInstance.myPrototype()) {
 *         ...
 *     }
 * </pre>
 * 
 * @author jkuhn
 * @since 1.0
 * @see Bean
 * 
 * @param <T> the actual type of the bean
 */
abstract class PrototypeModuleBean<T> extends AbstractModuleBean<T> {

	/**
	 * The bean logger.
	 */
	protected static final Logger LOGGER = Logger.getLogger(PrototypeModuleBean.class.getName());

	/**
	 * The list of instances issued by the bean.
	 */
	private Set<WeakReference<T>> instances;

	private ReferenceQueue<T> referenceQueue;

	/**
	 * <p>
	 * Creates a prototype module bean with the specified name.
	 * </p>
	 * 
	 * @param name the bean name
	 */
	public PrototypeModuleBean(String name) {
		super(name);
	}

	/**
	 * Expunges stake instances from the list.
	 */
	@SuppressWarnings("unchecked")
	private void expungeStaleInstances() {
		for (T ref; (ref = (T) this.referenceQueue.poll()) != null;) {
			this.instances.remove(ref);
		}
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
	public synchronized final void create() {
		if (this.instances == null) {
			LOGGER.info(() -> "Creating Prototype Bean " + (this.parent != null ? this.parent.getName() : "") + ":"
					+ this.name);
			this.instances = new HashSet<>();
			this.referenceQueue = new ReferenceQueue<T>();
			this.parent.recordBean(this);
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
		this.expungeStaleInstances();
		T instance = this.createInstance();
		WeakReference<T> reference = new WeakReference<>(instance, this.referenceQueue);
		this.instances.add(reference);

		return instance;
	}

	/**
	 * <p>
	 * Destroys the prototype bean and as a result all bean instances it has issued.
	 * </p>
	 * 
	 * <p>
	 * This method delegates bean instance destruction to the
	 * {@link #destroyInstance(Object)} method.
	 * </p>
	 */
	@Override
	public synchronized final void destroy() {
		if (this.instances != null) {
			LOGGER.info(() -> "Destroying Prototype Bean " + (this.parent != null ? this.parent.getName() : "") + ":"
					+ this.name);
			this.expungeStaleInstances();
			this.instances.stream().map(WeakReference::get).filter(Objects::nonNull)
					.forEach(instance -> this.destroyInstance(instance));
			this.instances.clear();
			this.instances = null;
		}
	}
}
