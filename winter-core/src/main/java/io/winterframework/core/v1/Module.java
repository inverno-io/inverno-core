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

import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.RetentionPolicy.CLASS;

import java.lang.annotation.Retention;
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
 * Generated module classes inherit this class which provides base module
 * lifecycle implementation.
 * </p>
 * 
 * <p>
 * The following describes module startup steps:
 * </p>
 * 
 * <ol>
 * <li>Start the required Winter modules included in the module.</li>
 * <li>Create the module beans that weren't already created.</li>
 * </ol>
 * 
 * <p>
 * Dependencies between beans determine the order in which they are created, as
 * a result a bean can be created before the enclosing module is actually
 * started.
 * </p>
 * 
 * <p>
 * The following describes the module destroy steps:
 * </p>
 * <ol>
 * <li>Destroy the module beans in the reverse creation order.</li>
 * <li>Stop the required Winter modules included in the module.</li>
 * </ol>
 * 
 * <p>
 * A module should always be built using a {@link ModuleBuilder}.
 * </p>
 * 
 * @author jkuhn
 * @since 1.0
 */
public abstract class Module {

	/**
	 * The module logger.
	 */
	private Logger logger = LogManager.getLogger(this.getClass());

	/**
	 * The module name.
	 */
	private String name;

	/**
	 * The parent module.
	 */
	private Module parent;

	/**
	 * The list of required Winter modules include in the module.
	 */
	private List<Module> modules;

	/**
	 * The list of beans in the module.
	 */
	private List<Bean<?>> beans;

	/**
	 * The bean stack used to track bean creation order.
	 */
	private Deque<Bean<?>> beansStack;

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
	 * Creates a module with the specified module linker and register it in this
	 * module.
	 * </p>
	 * 
	 * <p>
	 * A module can only be registered once to exactly one module.
	 * </p>
	 * 
	 * @param <T>          the type of the module to create
	 * @param moduleLinker the module linker to use to create the module.
	 * 
	 * @return the registered module.
	 */
	protected <T extends Module> T with(ModuleLinker<T> moduleLinker) {
		T module = moduleLinker.link();

		((Module) module).parent = this;
		this.modules.add(module);

		return module;
	}

	/**
	 * <p>
	 * Creates a module bean with the specified bean builder and registers it in
	 * this module.
	 * </p>
	 * 
	 * <p>
	 * A bean can only be registered once to exactly one module.
	 * </p>
	 * 
	 * @param <T>         the actual type of the bean
	 * @param beanBuilder the bean builder to use to create the bean
	 * 
	 * @return the registered bean
	 */
	protected <T> Bean<T> with(ModuleBeanBuilder<T> beanBuilder) {
		Bean<T> bean = beanBuilder.build();
		bean.parent = this;
		this.beans.add(bean);

		return bean;
	}
	
	/**
	 * <p>
	 * Creates a wrapper bean with the specified bean builder and registers it in
	 * this module.
	 * </p>
	 * 
	 * <p>
	 * A bean can only be registered once to exactly one module.
	 * </p>
	 * 
	 * @param <W>         the type of the wrapper bean
	 * @param <T>         the actual type of the bean
	 * @param beanBuilder the bean builder to use to create the bean
	 * 
	 * @return the registered bean
	 */
	protected <W extends Supplier<T>, T> Bean<T> with(WrapperBeanBuilder<W, T> beanBuilder) {
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
	 * Determines whether the module is active (ie. started).
	 * </p>
	 * 
	 * @return true if the module is active, false otherwise.
	 */
	public boolean isActive() {
		return active;
	}

	/**
	 * <p>
	 * Starts the module.
	 * </p>
	 * 
	 * <p>
	 * It creates and wires the beans defined within the module and the required
	 * Winter modules it includes, the bean dependency graph determines the order
	 * into which beans are created. When the module is stopped, beans are destroyed
	 * in the reverse order.
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
		this.logger.info("Starting Module " + this.name + "...");
		this.modules.stream().forEach(module -> module.start());
		this.beans.stream().forEach(bean -> bean.create());
		this.logger.info("Module {} started in {}ms", this.name, ((System.nanoTime() - t0) / 1000000));
//		this.logger.info(this.beansStack.stream().map(bean -> bean.name.toString()).collect(Collectors.joining(", "))); // TEST
	}

	/**
	 * <p>
	 * Stops the module.
	 * </p>
	 * 
	 * <p>
	 * This methods basically destroy the beans created during startup in the
	 * reverse order.
	 * </p>
	 */
	public void stop() {
		long t0 = System.nanoTime();
		this.logger.info("Stopping Module " + this.name + "...");
		this.beansStack.forEach(bean -> {
			try {
				bean.destroy();
			} catch (Exception e) {
				this.logger.warn("Error destroying Bean {}", () -> (bean.parent != null ? bean.parent.getName() + ":" : "") + bean.name);
			}
		});
		this.modules.stream().forEach(module -> module.stop());
		this.beansStack.clear();
		this.logger.info("Module {} stopped in {}ms", this.name, ((System.nanoTime() - t0) / 1000000));
		this.active = false;
	}

	/**
	 * <p>
	 * Aggregates single beans, collections of beans and arrays of beans.
	 * </p>
	 * 
	 * @author jkuhn
	 *
	 * @param <E> the bean type
	 */
	protected static class BeanAggregator<E> {

		private List<E> aggregate;

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
			this.aggregate.add(bean);
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
		public BeanAggregator<E> add(Collection<E> beans) {
			this.aggregate.addAll(beans);
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
			this.aggregate.addAll(Arrays.asList(beans));
			return this;
		}

		/**
		 * <p>
		 * Filters null bean and return a list representation of the aggregate.
		 * </p>
		 * 
		 * @return a list of beans
		 */
		public List<E> toList() {
			return this.aggregate.stream().filter(Objects::nonNull).collect(Collectors.toList());
		}

		/**
		 * <p>
		 * Filters null bean and return a set representation of the aggregate.
		 * </p>
		 * 
		 * @return a set of beans
		 */
		public Set<E> toSet() {
			return this.aggregate.stream().filter(Objects::nonNull).collect(Collectors.toSet());
		}

		/**
		 * <p>
		 * Filters null bean and return an array representation of the aggregate.
		 * </p>
		 * 
		 * @param generator a function which produces a new array of the desired type
		 *                  and the provided length
		 * 
		 * @return an array of beans
		 */
		public E[] toArray(IntFunction<E[]> generator) {
			return this.aggregate.stream().filter(Objects::nonNull).toArray(generator);
		}
	}

	/**
	 * <p>
	 * The Module Linker base class.
	 * </p>
	 * 
	 * <p>
	 * A Module linker is used within modules implementations to link a dependent
	 * modules into the current module. A linker is only used within a module and
	 * shall never be used directly.
	 * </p>
	 * 
	 * @param <T> the module type to link.
	 * 
	 * @author jkuhn
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
	 * @author jkuhn
	 * @since 1.0
	 */
	protected static abstract class ModuleBuilder<T extends Module> {

		/**
		 * <p>
		 * Creates a new Module Builder.
		 * </p>
		 * 
		 * @param nonOptionalSockets an even list of non-optional sockets pairs (name,
		 *                           value) required to build the module
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
			if (nullSockets.size() > 0) {
				throw new IllegalArgumentException("Following non-optional sockets are null: "
						+ nullSockets.stream().collect(Collectors.joining(", ")));
			}
		}

		/**
		 * <p>
		 * Builds the module.
		 * </p>
		 * 
		 * <p>
		 * This method actually delegates the actual module creation to the
		 * {@link #doBuild()} method.
		 * </p>
		 * 
		 * @return a new module instance
		 */
		public final T build() {
			T thisModule = this.doBuild();
			return thisModule;
		}

		/**
		 * <p>
		 * This method should be implemented by concrete implementation to return the
		 * actual module instance.
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
	 * {@link Bean} instances are used within modules during initialization to
	 * perform dependency injection in order to defer the actual beans instantiation
	 * beans at module startup. Dependency cycles, missing dependencies and other
	 * issues related to dependency injection are normally raised at compile time.
	 * </p>
	 * 
	 * <p>
	 * A bean has to be registered in a module before it can be used.
	 * </p>
	 * 
	 * @author jkuhn
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
		 * Returns the requested bean instance while making sure the enclosing module is
		 * active.
		 * </p>
		 * 
		 * @throws IllegalStateException if the enclosing module is inactive.
		 */
		@Override
		public final T get() throws IllegalStateException {
			if (!this.parent.isActive()) {
				throw new IllegalArgumentException("Module " + this.parent.getName() + " is inactive.");
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
	 * @author jkuhn
	 * @since 1.0
	 * @see Bean
	 * @see ModuleBeanBuilder
	 * @see WrapperBeanBuilder
	 * 
	 * @param <T> the actual type of the bean to build
	 * @param <B> the bean builder type to support method chaining
	 */
	protected interface BeanBuilder<T, B extends BeanBuilder<T,B>> {
		
		/**
		 * <p>
		 * Fallible consumer used to designates init and destroy methods which might
		 * throw checked exception.
		 * </p>
		 * 
		 * @author jkuhn
		 *
		 * @param <T> the type of the input to the operation
		 */
		@FunctionalInterface
		static interface FallibleConsumer<T> {
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
	 * A module {@link Bean} instance is built from a {@link Supplier} which is used
	 * to defer the actual instantiation of the bean. Initialization and destruction
	 * operations are invoked once the bean instance has been created (after
	 * dependency injection) and before its destruction respectively.
	 * </p>
	 * 
	 * <pre>
	 *     this.bean = ModuleBeanBuilder
	 *         .singleton("bean", () -&gt; {
	 *              BeanA beanA = new BeanA(serviceSocket.get());
	 *              return beanA;
	 *          })
	 *          .init(BeanA::init)
	 *          .destroy(BeanA::destroy)
	 *          .build(this);
	 * </pre>
	 * 
	 * @param <T> the actual type of the bean to build
	 * @param <B> the bean builder type to support method chaining
	 * 
	 * @author jkuhn
	 * @since 1.0
	 * @see Bean
	 */
	protected interface ModuleBeanBuilder<T> extends BeanBuilder<T, ModuleBeanBuilder<T>> {

		/**
		 * <p>
		 * Returns a singleton module bean builder.
		 * </p>
		 * 
		 * <p>
		 * Singleton {@link Bean}s are useful when one single instance of a bean should
		 * be injected through the application.
		 * </p>
		 * 
		 * @param <T>         the type of the bean to build
		 * @param beanName    the bean name
		 * @param constructor the bean instance supplier
		 * 
		 * @return a singleton Bean Builder
		 */
		static <T> ModuleBeanBuilder<T> singleton(String beanName, Supplier<T> constructor) {
			return new SingletonModuleBeanBuilder<T>(beanName, constructor);
		}
		
		/**
		 * <p>
		 * Returns a prototype module bean builder.
		 * </p>
		 * 
		 * <p>
		 * Prototype {@link Bean}s are useful when distinct instances of a bean should
		 * be injected though the application.
		 * </p>
		 * 
		 * @param <T>         the type of the bean to build
		 * @param beanName    the bean name
		 * @param constructor the bean instance supplier
		 * 
		 * @return a prototype Bean Builder
		 */
		static <T> ModuleBeanBuilder<T> prototype(String beanName, Supplier<T> constructor) {
			return new PrototypeModuleBeanBuilder<T>(beanName, constructor);
		}
		
		/**
		 * <p>
		 * Builds the bean.
		 * </p>
		 * 
		 * @return a bean
		 */
		Bean<T> build();
		
		/**
		 * <p>
		 * Specifies an override that, when present, provides bean instances instead of the builder.
		 * </p>
		 * 
		 * @param override An optional override
		 * @return this builder
		 */
		ModuleBeanBuilder<T> override(Optional<Supplier<T>> override);
	}
	
	/**
	 * <p>
	 * A BeanBuilder for creating wrapper {@link Bean} instances.
	 * </p>
	 * 
	 * <p>
	 * A wrapper {@link Bean} instance is built from a {@link Supplier} used to
	 * obtain a wrapper instance which wraps the actual bean instance creation,
	 * initialization and destruction logic and defer the actual instantiation of
	 * the bean. Initialization and destruction operations are invoked on the
	 * wrapper instance (after dependency injection) and before its destruction
	 * respectively.
	 * </p>
	 * 
	 * <p>
	 * Particular care must be taken when a prototype wrapper bean is created as the
	 * wrapper bean instance must not hold any strong reference to the actual bean
	 * instance in order to prevent memory leaks. In that case, using a
	 * {@link WeakReference} is strongly advised.
	 * </p>
	 * 
	 * <pre>
	 *     this.bean = WrapperBeanBuilder
	 *         .singleton("bean", () -&gt; {
	 *              BeanA beanA = new BeanA(serviceSocket.get());
	 *              return beanA;
	 *          })
	 *          .init(BeanA::init)
	 *          .destroy(BeanA::destroy)
	 *          .build(this);
	 * </pre>
	 * 
	 * @param <T> the actual type of the bean to build
	 * @param <B> the bean builder type to support method chaining
	 * 
	 * @author jkuhn
	 * @since 1.0
	 * @see Bean
	 */
	protected interface WrapperBeanBuilder<W extends Supplier<T>, T> extends BeanBuilder<W, WrapperBeanBuilder<W, T>> {

		/**
		 * <p>
		 * Returns a singleton wrapper bean builder.
		 * </p>
		 * 
		 * <p>
		 * Singleton {@link Bean}s are useful when one single instance of a bean should
		 * be injected through the application.
		 * </p>
		 * 
		 * @param <T>         the type of the bean to build
		 * @param beanName    the bean name
		 * @param constructor the bean instance supplier
		 * 
		 * @return a singleton Bean Builder
		 */
		static <W extends Supplier<T>, T> WrapperBeanBuilder<W, T> singleton(String beanName, Supplier<W> constructor) {
			return new SingletonWrapperBeanBuilder<>(beanName, constructor);
		}

		/**
		 * <p>
		 * Returns a prototype wrapper bean builder.
		 * </p>
		 * 
		 * <p>
		 * Prototype {@link Bean}s are useful when distinct instances of a bean should
		 * be injected though the application.
		 * </p>
		 * 
		 * @param <T>         the type of the bean to build
		 * @param beanName    the bean name
		 * @param constructor the bean instance supplier
		 * 
		 * @return a prototype Bean Builder
		 */
		static <W extends Supplier<T>, T> WrapperBeanBuilder<W, T> prototype(String beanName, Supplier<W> constructor) {
			return new PrototypeWrapperBeanBuilder<>(beanName, constructor);
		}
		
		/**
		 * <p>
		 * Builds the bean.
		 * </p>
		 * 
		 * @return a bean
		 */
		public Bean<T> build();
		
		/**
		 * <p>
		 * Specifies an override that, when present, provides bean instances instead of the builder.
		 * </p>
		 * 
		 * @param override An optional override
		 * @return this builder
		 */
		WrapperBeanBuilder<W, T> override(Optional<Supplier<T>> override);
	}
	
	/**
	 * <p>
	 * Provides socket information to the Winter compiler.
	 * </p>
	 * 
	 * <p>
	 * These information are necessary for the compiler to be able to load component
	 * modules in a module while preserving socket names and preventing dependency
	 * cycles.
	 * </p>
	 * 
	 * @author jkuhn
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
