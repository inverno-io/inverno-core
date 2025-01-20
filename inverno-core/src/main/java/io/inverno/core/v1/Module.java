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

import static java.lang.annotation.ElementType.PARAMETER;
import java.lang.annotation.Retention;
import static java.lang.annotation.RetentionPolicy.CLASS;
import java.lang.annotation.Target;
import java.lang.ref.WeakReference;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Deque;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.IntFunction;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * <p>
 * The Module base class.
 * </p>
 *
 * <p>
 * Generated module classes inherit this class which provides base module lifecycle implementation.
 * </p>
 *
 * <p>
 * The following describes module startup steps:
 * </p>
 *
 * <ol>
 * <li>Start the required Inverno modules included in the module.</li>
 * <li>Create the module beans that weren't already created.</li>
 * </ol>
 *
 * <p>
 * Dependencies between beans determine the order in which they are created, as a result a bean can be created before the enclosing module is actually started.
 * </p>
 *
 * <p>
 * The following describes the module destroy steps:
 * </p>
 * <ol>
 * <li>Destroy the module beans in the reverse creation order.</li>
 * <li>Stop the required Inverno modules included in the module.</li>
 * </ol>
 *
 * <p>
 * A module should always be built using a {@link ModuleBuilder}.
 * </p>
 *
 * @author <a href="mailto:jeremy.kuhn@inverno.io">Jeremy Kuhn</a>
 * @since 1.0
 */
public abstract class Module {

	/**
	 * The module logger.
	 */
	private final Logger logger = LogManager.getLogger(this.getClass());

	/**
	 * The module name.
	 */
	private final String name;

	/**
	 * The list of required Inverno modules include in the module.
	 */
	private final List<Module> modules;

	/**
	 * The list of beans in the module.
	 */
	private final List<Bean<?>> beans;

	/**
	 * The bean stack used to track bean creation order.
	 */
	private final Deque<Bean<?>> beansStack;

	/**
	 * The parent module.
	 */
	private Module parent;
	
	/**
	 * THe module's state
	 */
	private boolean active;

	/**
	 * <p>
	 * Creates a new Module with the specified name.
	 * </p>
	 * 
	 * @param moduleName the module name
	 */
	protected Module(String moduleName) {
		this.name = moduleName;
		this.beans = new ArrayList<>();
		this.beansStack = new ArrayDeque<>();
		this.modules = new ArrayList<>();
	}

	/**
	 * <p>
	 * Records a bean creation into the module.
	 * </p>
	 * 
	 * @param bean the bean to record
	 */
	void recordBean(Bean<?> bean) {
		// Beans must be recorded as they are created
		if (this.parent != null) {
			this.parent.recordBean(bean);
		} else {
			this.beansStack.push(bean);
		}
	}

	/**
	 * <p>
	 * Creates a module with the specified module linker and register it in this module.
	 * </p>
	 *
	 * <p>
	 * A module can only be registered once to exactly one module.
	 * </p>
	 *
	 * @param <T>          the type of the module to create
	 * @param moduleLinker the module linker to use to create the module
	 *
	 * @return the registered module
	 */
	protected <T extends Module> T with(ModuleLinker<T> moduleLinker) {
		T module = moduleLinker.link();

		((Module) module).parent = this;
		this.modules.add(module);

		return module;
	}

	/**
	 * <p>
	 * Creates a module bean with the specified bean builder and registers it in this module.
	 * </p>
	 *
	 * <p>
	 * A bean can only be registered once to exactly one module.
	 * </p>
	 *
	 * @param <T>         the type of the bean
	 * @param beanBuilder the bean builder to use to create the bean
	 *
	 * @return the registered bean
	 */
	protected <T> Bean<T> with(ModuleBeanBuilder<T, ?> beanBuilder) {
		Bean<T> bean = beanBuilder.build();
		bean.parent = this;
		this.beans.add(bean);

		return bean;
	}
	
	/**
	 * <p>
	 * Creates a wrapper bean with the specified bean builder and registers it in this module.
	 * </p>
	 *
	 * <p>
	 * A bean can only be registered once to exactly one module.
	 * </p>
	 *
	 * @param <T>         the type of the bean
	 * @param beanBuilder the bean builder to use to create the bean
	 *
	 * @return the registered bean
	 */
	protected <T> Bean<T> with(WrapperBeanBuilder<T, ?, ?> beanBuilder) {
		Bean<T> bean = beanBuilder.build();
		bean.parent = this;
		this.beans.add(bean);

		return bean;
	}

	/**
	 * <p>
	 * Returns the name of the module.
	 * </p>
	 * 
	 * @return the name of the module
	 */
	public String getName() {
		return this.name;
	}

	/**
	 * <p>
	 * Determines whether the module is active (i.e. started).
	 * </p>
	 * 
	 * @return true if the module is active, false otherwise.
	 */
	public boolean isActive() {
		return active;
	}
	
	/**
	 * <p>
	 * Determines whether this module or one of its ancestors is active which would indicate that this module is a component module in a tree of module being started.
	 * </p>
	 *
	 * @return true if an ancestor module is active, false otherwise.
	 */
	private boolean isSuperActive() {
		return this.active || (this.parent != null && this.parent.isSuperActive());
	}

	/**
	 * <p>
	 * Starts the module.
	 * </p>
	 *
	 * <p>
	 * It creates and wires the beans defined within the module and the required Inverno modules it includes, the bean dependency graph determines the order into which beans are created. When the
	 * module is stopped, beans are destroyed in the reverse order.
	 * </p>
	 *
	 * @throws IllegalStateException if the module is active.
	 */
	public void start() throws IllegalStateException {
		if (this.isActive()) {
			throw new IllegalStateException("Module " + this.name + " is already active");
		}
		this.active = true;
		long t0 = System.nanoTime();
		this.logger.info("Starting Module {}...", () -> this.name);
		this.modules.stream().filter(module -> !module.isActive()).forEach(Module::start);
		this.beans.forEach(Bean::create);
		this.logger.info("Module {} started in {}ms", () -> this.name, () -> ((System.nanoTime() - t0) / 1000000));
	}

	/**
	 * <p>
	 * Stops the module.
	 * </p>
	 *
	 * <p>
	 * This method basically destroys the beans created during startup in the reverse order.
	 * </p>
	 */
	public void stop() {
		if(this.isActive()) {
			long t0 = System.nanoTime();
			this.logger.info("Stopping Module {}...", () -> this.name);
			this.beansStack.forEach(bean -> {
				long bean_t0 = System.nanoTime();
				try {
					bean.destroy();
				} 
				catch (Exception e) {
					this.logger.warn("Error destroying Bean {}", () -> (bean.parent != null ? bean.parent.getName() + ":" : "") + bean.name);
				}
				finally {
					this.logger.debug("Bean {} destroyed in {}ms", () -> (bean.parent != null ? bean.parent.getName() + ":" : "") + bean.name, () -> ((System.nanoTime() - bean_t0) / 1000000));
				}
			});
			this.modules.forEach(Module::stop);
			this.beansStack.clear();
			this.logger.info("Module {} stopped in {}ms", () -> this.name, () -> ((System.nanoTime() - t0) / 1000000));
			this.active = false;
		}
	}
	
	/**
	 * <p>
	 * Aggregates single beans, collections of beans and arrays of beans.
	 * </p>
	 * 
	 * @author <a href="mailto:jeremy.kuhn@inverno.io">Jeremy Kuhn</a>
	 *
	 * @param <E> the bean type
	 */
	protected static class BeanAggregator<E> {

		private final List<E> aggregate;

		/**
		 * Creates an aggregator.
		 */
		public BeanAggregator() {
			this.aggregate = new ArrayList<>();
		}

		/**
		 * <p>
		 * Appends the specified bean to the aggregate.
		 * </p>
		 * 
		 * @param bean the bean to add.
		 * 
		 * @return the aggregator instance.
		 */
		public BeanAggregator<E> add(E bean) {
			if(bean != null) {
				this.aggregate.add(bean);
			}
			return this;
		}

		/**
		 * <p>
		 * Appends the specified collection of beans to the aggregate.
		 * </p>
		 * 
		 * @param beans the beans to add.
		 * 
		 * @return the aggregator instance.
		 */
		public BeanAggregator<E> add(Collection<? extends E> beans) {
			if(beans != null) {
				this.aggregate.addAll(beans);
			}
			return this;
		}

		/**
		 * <p>
		 * Appends the specified array of beans to the aggregate.
		 * </p>
		 * 
		 * @param beans the beans to add.
		 * 
		 * @return the aggregator instance.
		 */
		public BeanAggregator<E> add(E[] beans) {
			if(beans != null) {
				this.aggregate.addAll(Arrays.asList(beans));
			}
			return this;
		}

		/**
		 * <p>
		 * Filters null beans and returns a list representation of the aggregate.
		 * </p>
		 * 
		 * @return a list of beans
		 */
		public List<E> toList() {
			return this.aggregate.stream().filter(Objects::nonNull).collect(Collectors.toList());
		}
		
		/**
		 * <p>
		 * Filters null beans and returns a list representation of the aggregate or an empty optional if the aggregate is empty.
		 * </p>
		 * 
		 * @return an optional containing the aggregate as a list or an empty optional
		 */
		public Optional<List<E>> toOptionalList() {
			return Optional.of(this.aggregate.stream().filter(Objects::nonNull).collect(Collectors.toList())).filter(list -> !list.isEmpty());
		}

		/**
		 * <p>
		 * Filters null beans and returns a set representation of the aggregate.
		 * </p>
		 * 
		 * @return a set of beans
		 */
		public Set<E> toSet() {
			return this.aggregate.stream().filter(Objects::nonNull).collect(Collectors.toSet());
		}

		/**
		 * <p>
		 * Filters null beans and returns a set representation of the aggregate or an empty optional if the aggregate is empty.
		 * </p>
		 * 
		 * @return an optional containing the aggregate as a set or an empty optional
		 */
		public Optional<Set<E>> toOptionalSet() {
			return Optional.of(this.aggregate.stream().filter(Objects::nonNull).collect(Collectors.toSet())).filter(set -> !set.isEmpty());
		}
		
		/**
		 * <p>
		 * Filters null beans and returns an array representation of the aggregate.
		 * </p>
		 *
		 * @param generator a function which produces a new array of the desired type and the provided length
		 *
		 * @return an array of beans
		 */
		public E[] toArray(IntFunction<E[]> generator) {
			return this.aggregate.stream().filter(Objects::nonNull).toArray(generator);
		}
		
		/**
		 * <p>
		 * Filters null beans and returns an array representation of the aggregate or an empty optional if the aggregate is empty.
		 * </p>
		 * 
		 * @param generator a function which produces a new array of the desired type and the provided length
		 * 
		 * @return an optional containing the aggregate as an array or an empty optional
		 */
		public Optional<E[]> toOptionalArray(IntFunction<E[]> generator) {
			return Optional.of(this.aggregate.stream().filter(Objects::nonNull).toArray(generator)).filter(array -> array.length > 0);
		}
	}

	/**
	 * <p>
	 * The Module Linker base class.
	 * </p>
	 *
	 * <p>
	 * A Module linker is used within modules implementations to link a dependent modules into the current module. A linker is only used within a module and shall never be used directly.
	 * </p>
	 *
	 * @param <T> the module type to link.
	 *
	 * @author <a href="mailto:jeremy.kuhn@inverno.io">Jeremy Kuhn</a>
	 * @since 1.0
	 */
	protected static abstract class ModuleLinker<T extends Module> {

		/**
		 * The socket map to be used during linking process.
		 */
		protected Map<String, Object> sockets;

		/**
		 * <p>
		 * Creates a new Module linker with the specified socket map.
		 * </p>
		 * 
		 * @param sockets the socket map
		 */
		public ModuleLinker(Map<String, Object> sockets) {
			this.sockets = sockets;
		}

		/**
		 * <p>
		 * Links the socket map in a new module instance and return that instance.
		 * </p>
		 * 
		 * @return a new linked module instance
		 */
		protected abstract T link();
	}

	/**
	 * <p>
	 * The Module Builder base class.
	 * </p>
	 * 
	 * <p>
	 * All module have to be built by a builder.
	 * </p>
	 * 
	 * @param <T> the module type to build.
	 * 
	 * @author <a href="mailto:jeremy.kuhn@inverno.io">Jeremy Kuhn</a>
	 * @since 1.0
	 */
	protected static abstract class ModuleBuilder<T extends Module> {

		/**
		 * <p>
		 * Creates a new Module Builder.
		 * </p>
		 *
		 * @param nonOptionalSockets an even list of non-optional sockets pairs (name, value) required to build the module
		 *
		 * @throws IllegalArgumentException if one or more non-optional sockets are null
		 */
		public ModuleBuilder(Object... nonOptionalSockets) throws IllegalArgumentException {
			if (nonOptionalSockets.length % 2 != 0) {
				throw new IllegalArgumentException("Invalid list of required socket");
			}
			List<String> nullSockets = new ArrayList<>();
			for (int i = 0; i < nonOptionalSockets.length; i = i + 2) {
				if (nonOptionalSockets[i + 1] == null) {
					nullSockets.add(nonOptionalSockets[i].toString());
				}
			}
			if (!nullSockets.isEmpty()) {
				throw new IllegalArgumentException("Following non-optional sockets are null: "
						+ String.join(", ", nullSockets));
			}
		}

		/**
		 * <p>
		 * Builds the module.
		 * </p>
		 *
		 * <p>
		 * This method actually delegates the actual module creation to the {@link #doBuild()} method.
		 * </p>
		 *
		 * @return a new module instance
		 */
		public final T build() {
			return this.doBuild();
		}

		/**
		 * <p>
		 * This method should be implemented by concrete implementation to return the actual module instance.
		 * </p>
		 *
		 * @return a new module instance
		 */
		protected abstract T doBuild();
	}

	/**
	 * <p>
	 * Interface representing the lifecycle of a bean in a module.
	 * </p>
	 *
	 * <p>
	 * {@link Bean} instances are used within modules during initialization to perform dependency injection in order to defer the actual beans instantiation beans at module startup. Dependency cycles,
	 * missing dependencies and other issues related to dependency injection are normally raised at compile time.
	 * </p>
	 *
	 * <p>
	 * A bean has to be registered in a module before it can be used.
	 * </p>
	 *
	 * @author <a href="mailto:jeremy.kuhn@inverno.io">Jeremy Kuhn</a>
	 * @since 1.0
	 * @see BeanBuilder
	 *
	 * @param <T> the actual type of the bean
	 */
	protected static abstract class Bean<T> implements Supplier<T> {

		/**
		 * The module into which the bean is registered.
		 */
		protected Module parent;

		/**
		 * The bean name.
		 */
		protected String name;

		/**
		 * <p>
		 * Creates a bean with the specified name.
		 * </p>
		 * 
		 * @param name the bean name
		 */
		protected Bean(String name) {
			this.name = name;
		}

		/**
		 * <p>
		 * Returns the requested bean instance while making sure the enclosing module is active.
		 * </p>
		 *
		 * <p>
		 * In case the enclosing module is not active but one of its ancestors is active, this method starts the enclosing module in order to start modules in their natural order. If no ancestor is
		 * active (i.e. the enclosing module is not part of a module initialization process), an {@link IllegalStateException} is thrown.
		 * </p>
		 *
		 * @throws IllegalStateException if the enclosing module is inactive and not part of a module initialization process.
		 */
		@Override
		public final T get() throws IllegalStateException {
			if (!this.parent.isActive()) {
				if(this.parent.isSuperActive()) {
					this.parent.start();
				}
				else {
					throw new IllegalArgumentException("Module " + this.parent.getName() + " is inactive.");
				}
			}
			return this.doGet();
		}

		/**
		 * <p>
		 * Returns the supplied bean instance.
		 * </p>
		 * 
		 * @return a bean instance
		 */
		public abstract T doGet();

		/**
		 * <p>
		 * Creates the underlying bean instance
		 * </p>
		 */
		public abstract void create();

		/**
		 * <p>
		 * Destroys the underlying instance.
		 * </p>
		 */
		public abstract void destroy();
	}

	/**
	 * <p>
	 * A BeanBuilder is used within a module class to create {@link Bean} instances.
	 * </p>
	 *
	 * <p>
	 * A BeanBuilder is always created for a specific module instance.
	 * </p>
	 *
	 * @author <a href="mailto:jeremy.kuhn@inverno.io">Jeremy Kuhn</a>
	 * @since 1.0
	 * 
	 * @see Bean
	 * @see ModuleBeanBuilder
	 * @see WrapperBeanBuilder
	 *
	 * @param <T> the actual type of the bean to build
	 * @param <B> the bean builder type to support method chaining
	 */
	protected interface BeanBuilder<T, B extends BeanBuilder<T, B>> {
		
		/**
		 * <p>
		 * Fallible consumer used to designates init and destroy methods which might throw checked exception.
		 * </p>
		 *
		 * @author <a href="mailto:jeremy.kuhn@inverno.io">Jeremy Kuhn</a>
		 *
		 * @param <T> the type of the input to the operation
		 */
		@FunctionalInterface
		interface FallibleConsumer<T> {
			
			/**
			 * <p>
			 * Performs this operation on the given argument.
			 * </p>
			 *
			 * @param t the input argument
			 *
			 * @throws Exception if something goes wrong processing the argument
			 */
			void accept(T t) throws Exception;
		}
		
		/**
		 * <p>
		 * Adds a bean initialization operation.
		 * </p>
		 * 
		 * @param init the bean initialization operation.
		 * 
		 * @return this builder
		 */
		B init(FallibleConsumer<T> init);

		/**
		 * <p>
		 * Adds a bean destruction operation.
		 * </p>
		 * 
		 * @param destroy the bean destruction operation.
		 * 
		 * @return this builder
		 */
		B destroy(FallibleConsumer<T> destroy);
	}
	
	/**
	 * <p>
	 * A BeanBuilder for creating module {@link Bean} instances.
	 * </p>
	 *
	 * <p>
	 * A module {@link Bean} instance is built from a {@link Supplier} which is used to defer the actual instantiation of the bean. Initialization and destruction operations are invoked once the bean
	 * instance has been created (after dependency injection) and before its destruction respectively.
	 * </p>
	 *
	 * <pre>{@code
	 * this.bean = ModuleBeanBuilder
	 *     .singleton("bean", () -> {
	 *          BeanA beanA = new BeanA(serviceSocket.get());
	 *          return beanA;
	 *      })
	 *      .init(BeanA::init)
	 *      .destroy(BeanA::destroy)
	 *      .build(this);
	 * }</pre>
	 *
	 * @author <a href="mailto:jeremy.kuhn@inverno.io">Jeremy Kuhn</a>
	 * @since 1.0
	 * @see Bean
	 *
	 * @param <P> the type provided by the bean to build
	 * @param <T> the actual type of the bean to build
	 */
	protected interface ModuleBeanBuilder<P, T> extends BeanBuilder<T, ModuleBeanBuilder<P, T>> {

		/**
		 * <p>
		 * Returns a singleton module bean builder.
		 * </p>
		 *
		 * <p>
		 * Singleton {@link Bean}s are useful when one single instance of a bean should be injected through the application.
		 * </p>
		 *
		 * @param <T>         the type of the bean to build
		 * @param beanName    the bean name
		 * @param constructor the bean instance supplier
		 *
		 * @return a singleton Bean Builder
		 */
		static <T> ModuleBeanBuilder<T, T> singleton(String beanName, Supplier<T> constructor) {
			return new SingletonModuleBeanBuilder<>(beanName, constructor);
		}
		
		/**
		 * <p>
		 * Returns a prototype module bean builder.
		 * </p>
		 *
		 * <p>
		 * Prototype {@link Bean}s are useful when distinct instances of a bean should be injected though the application.
		 * </p>
		 *
		 * @param <T>         the type of the bean to build
		 * @param beanName    the bean name
		 * @param constructor the bean instance supplier
		 *
		 * @return a prototype Bean Builder
		 */
		static <T> ModuleBeanBuilder<T, T> prototype(String beanName, Supplier<T> constructor) {
			return new PrototypeModuleBeanBuilder<>(beanName, constructor);
		}
		
		/**
		 * <p>
		 * Builds the bean.
		 * </p>
		 * 
		 * @return a bean
		 */
		Bean<P> build();
		
		/**
		 * <p>
		 * Specifies an override that, when present, provides bean instances instead of the builder.
		 * </p>
		 * 
		 * @param <U>      the type provided by the bean to build
		 * @param override an optional override
		 * 
		 * @return this builder
		 */
		<U> ModuleBeanBuilder<U, T> override(Optional<Supplier<U>> override);
	}
	
	/**
	 * <p>
	 * A BeanBuilder for creating wrapper {@link Bean} instances.
	 * </p>
	 *
	 * <p>
	 * A wrapper {@link Bean} instance is built from a {@link Supplier} used to obtain a wrapper instance which wraps the actual bean instance creation, initialization and destruction logic and defer
	 * the actual instantiation of the bean. Initialization and destruction operations are invoked on the wrapper instance (after dependency injection) and before its destruction respectively.
	 * </p>
	 *
	 * <p>
	 * Particular care must be taken when a prototype wrapper bean is created as the wrapper bean instance must not hold any strong reference to the actual bean instance in order to prevent memory
	 * leaks. In that case, using a {@link WeakReference} is strongly advised.
	 * </p>
	 *
	 * <pre>{@code
	 * this.bean = WrapperBeanBuilder
	 *     .singleton("bean", () -> {
	 *          BeanA beanA = new BeanA(serviceSocket.get());
	 *          return beanA;
	 *      })
	 *      .init(BeanA::init)
	 *      .destroy(BeanA::destroy)
	 *      .build(this);
	 * }</pre>
	 *
	 * @author <a href="mailto:jeremy.kuhn@inverno.io">Jeremy Kuhn</a>
	 * @since 1.0
	 * @see Bean
	 * 
	 * @param <P> the type provided by the bean to build
	 * @param <T> the actual type of the bean to build
	 * @param <W> the bean wrapper which supplies the bean instance
	 */
	protected interface WrapperBeanBuilder<P, T, W extends Supplier<T>> extends BeanBuilder<W, WrapperBeanBuilder<P, T, W>> {

		/**
		 * <p>
		 * Returns a singleton wrapper bean builder.
		 * </p>
		 *
		 * <p>
		 * Singleton {@link Bean}s are useful when one single instance of a bean should be injected through the application.
		 * </p>
		 *
		 * @param <T>         the type of the bean to build
		 * @param <W>         the bean wrapper which supplies the bean instance
		 * @param beanName    the bean name
		 * @param constructor the bean instance supplier
		 *
		 * @return a singleton Bean Builder
		 */
		static <T, W extends Supplier<T>> WrapperBeanBuilder<T, T, W> singleton(String beanName, Supplier<W> constructor) {
			return new SingletonWrapperBeanBuilder<>(beanName, constructor);
		}

		/**
		 * <p>
		 * Returns a prototype wrapper bean builder.
		 * </p>
		 *
		 * <p>
		 * Prototype {@link Bean}s are useful when distinct instances of a bean should be injected though the application.
		 * </p>
		 *
		 * @param <T>         the type of the bean to build
		 * @param <W>         the bean wrapper which supplies the bean instance
		 * @param beanName    the bean name
		 * @param constructor the bean instance supplier
		 *
		 * @return a prototype Bean Builder
		 */
		static <T, W extends Supplier<T>> WrapperBeanBuilder<T, T, W> prototype(String beanName, Supplier<W> constructor) {
			return new PrototypeWrapperBeanBuilder<>(beanName, constructor);
		}
		
		/**
		 * <p>
		 * Builds the bean.
		 * </p>
		 * 
		 * @return a bean
		 */
		Bean<P> build();
		
		/**
		 * <p>
		 * Specifies an override that, when present, provides bean instances instead of the builder.
		 * </p>
		 *
		 * @param <U>      the type provided by the override and therefore the bean to build
		 * @param override An optional override
		 *
		 * @return this builder
		 */
		<U> WrapperBeanBuilder<U, T, W> override(Optional<Supplier<U>> override);
	}
	
	/**
	 * <p>
	 * Provides socket information to the Inverno compiler.
	 * </p>
	 *
	 * <p>
	 * This information is necessary for the compiler to be able to load component modules in a module while preserving socket names and preventing dependency cycles.
	 * </p>
	 *
	 * @author <a href="mailto:jeremy.kuhn@inverno.io">Jeremy Kuhn</a>
	 *
	 */
	@Retention(CLASS)
	@Target(PARAMETER)
	protected @interface Socket {
		
		/**
		 * <p>
		 * Indicates the name of the socket, defaults to the name of the socket class.
		 * </p>
		 * 
		 * @return A name
		 */
		String name() default "";
		
		/**
		 * <p>
		 * Indicates the name of the beans in the module a socket bean is wired to.
		 * </p>
		 * 
		 * @return A list of bean names
		 */
		String[] wiredTo() default {};
	}
}
