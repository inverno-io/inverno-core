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

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Deque;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.IntFunction;
import java.util.function.Supplier;
import java.util.logging.Logger;
import java.util.stream.Collectors;

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
 * <li>Write the banner to the log output if the module is a root module and a banner has been set.</li>
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
	private Logger logger = Logger.getLogger(this.getClass().getName());
	
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
	 * The module banner.
	 */
	private Banner banner;
	
	/**
	 * <p>
	 * Create a new Module with the specified name.
	 * </p>
	 * 
	 * @param moduleName
	 *            The module name
	 */
	protected Module(String moduleName) {
		this.name = moduleName;
		this.beans = new ArrayList<>();
		this.beansStack = new ArrayDeque<>();
		this.modules = new ArrayList<>();
	}

	/**
	 * <p>
	 * Record a bean creation into the module.
	 * </p>
	 * 
	 * @param bean The bean to record
	 */
	void recordBean(Bean<?> bean) {
		// Beans must be recorded as they are created
		if(this.parent != null) {
			this.parent.recordBean(bean);
		}
		else {
			this.beansStack.push(bean);
		}
	}
	
	/**
	 * <p>
	 * Set the module banner to be written to the log output if the module is a root
	 * module.
	 * </p>
	 * 
	 * @param banner The banner to set
	 */
	void setBanner(Banner banner) {
		this.banner = banner;
	}

	/**
	 * <p>
	 * Determine whether the banner should be displayed, only root module can
	 * display a banner.
	 * </p>
	 * 
	 * @return true when the banner is displayed
	 */
	boolean isBannerVisible() {
		return this.banner != null && this.parent == null;
	}

	/**
	 * <p>
	 * Create a module with the specified module linker and register it in this
	 * module.
	 * </p>
	 * 
	 * <p>
	 * A module can only be registered once to exactly one module.
	 * </p>
	 * 
	 * @param moduleLinker The module linker to use to create the module.
	 * 
	 * @return The registered module.
	 */
	protected <T extends Module> T with(ModuleLinker<T> moduleLinker) {
		T module = moduleLinker.link();
		
		((Module)module).parent = this;
		this.modules.add(module);
		
		return module;
	}

	/**
	 * <p>
	 * Create a bean with the specified bean builder and register it in this module.
	 * </p>
	 * 
	 * <p>
	 * A bean can only be registered once to exactly one module.
	 * </p>
	 * 
	 * @param beanBuilder The bean builder to use to create the bean
	 * @return The registered bean
	 */
	protected <T> Bean<T> with(BeanBuilder<T> beanBuilder) {
		Bean<T> bean = beanBuilder.build();
		bean.parent = this;
		this.beans.add(bean);
		
		return bean;
	}

	/**
	 * <p>
	 * Return the name of the module.
	 * </p>
	 * 
	 * @return The name of the module
	 */
	public String getName() {
		return this.name;
	}
		
	/**
	 * <p>
	 * Start the module.
	 * </p>
	 * 
	 * <p>
	 * This method displays banner to the log output when the module is a root
	 * module (ie. a module with no parent) and a banner is set.
	 * </p>
	 * 
	 * <p>
	 * It creates and wires the beans defined within the module and the required
	 * Winter modules it includes, the bean dependency graph determines the order
	 * into which beans are created. When the module is stopped, beans are detroyed
	 * in the reverse order.
	 * </p>
	 */
	public void start() {
		if(this.isBannerVisible()) {
			this.logger.info(() -> {
				ByteArrayOutputStream bannerStream = new ByteArrayOutputStream();
				this.banner.print(new PrintStream(bannerStream));
				return bannerStream.toString();	
			});
		}
		long t0 = System.nanoTime();
		this.logger.info("Starting Module " + this.name + "...");
		this.modules.stream().forEach(module -> module.start());
		this.beans.stream().forEach(bean -> bean.create());
		this.logger.info("Module " + this.name + " started in " + ((System.nanoTime() - t0) / 1000000) + "ms");
//		this.logger.info(this.beansStack.stream().map(bean -> bean.name.toString()).collect(Collectors.joining(", "))); // TEST
	}

	/**
	 * <p>
	 * Stop the module.
	 * </p>
	 * 
	 * </p>
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
			}
			catch(Exception e) {
				this.logger.warning("Error destroying Bean " + (bean.parent != null ? bean.parent.getName() : "")  + ":" + bean.name);
			}
		});
		this.modules.stream().forEach(module -> module.stop());
		this.beansStack.clear();
		this.logger.info("Module " + this.name + " stopped in " + ((System.nanoTime() - t0) / 1000000) + "ms");
	}
	
	/**
	 * <p>
	 * Aggregate single beans, collections of beans and arrays of beans.
	 * </p>
	 * @author jkuhn
	 *
	 * @param <E> The bean type 
	 */
	protected static class BeanAggregator<E> {
		
		private List<E> aggregate;
		
		/**
		 * Create an aggregator.
		 */
		public BeanAggregator() {
			this.aggregate = new ArrayList<>();
		}
		
		/**
		 * <p>
		 * Append the specified bean to the aggregate.
		 * </p>
		 * 
		 * @param bean The bean to add.
		 * 
		 * @return The aggregator instance.
		 */
		public BeanAggregator<E> add(E bean) {
			this.aggregate.add(bean);
			return this;
		}
		
		/**
		 * <p>
		 * Append the specified collection of beans to the aggregate.
		 * </p>
		 * 
		 * @param beans The beans to add.
		 * 
		 * @return The aggregator instance.
		 */
		public BeanAggregator<E> add(Collection<E> beans) {
			this.aggregate.addAll(beans);
			return this;
		}
		
		/**
		 * <p>
		 * Append the specified array of beans to the aggregate.
		 * </p>
		 * 
		 * @param beans The beans to add.
		 * 
		 * @return The aggregator instance.
		 */
		public BeanAggregator<E> add(E[] beans) {
			this.aggregate.addAll(Arrays.asList(beans));
			return this;
		}
		
		/**
		 * <p>
		 * Filter null bean and return a list representation of the aggregate.
		 * </p>
		 * 
		 * @return A list of beans
		 */
		public List<E> toList() {
			return this.aggregate.stream().filter(Objects::nonNull).collect(Collectors.toList());
		}
		
		/**
		 * <p>
		 * Filter null bean and return a set representation of the aggregate.
		 * </p>
		 * 
		 * @return A set of beans
		 */
		public Set<E> toSet() {
			return this.aggregate.stream().filter(Objects::nonNull).collect(Collectors.toSet());
		}
		
		/**
		 * <p>
		 * Filter null bean and return an array representation of the aggregate.
		 * </p>
		 * 
		 * @return An array of beans
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
	 * @param <T> The module type to link.
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
		 * Create a new Module linker with the specified socket map.
		 * </p>
		 * 
		 * @param sockets The socket map
		 */
		public ModuleLinker(Map<String, Object> sockets) {
			this.sockets = sockets;
		}

		/**
		 * </p>
		 * Link the socket map in a new module instance and return that instance.
		 * </p>
		 * 
		 * @return A new linked module instance
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
	 * @param <T> The module type to build.
	 * 
	 * @author jkuhn
	 * @since 1.0
	 */
	protected static abstract class ModuleBuilder<T extends Module> {
		
		/**
		 * The module banner
		 */
		private Banner banner;
		
		/**
		 * <p>
		 * Create a new Module Builder.
		 * </p>
		 * 
		 * @param nonOptionalSockets
		 *            An even list of non-optional sockets pairs (name, value) required to build the module
		 * 
		 * @throws IllegalArgumentException If one or more non-optional sockets are null
		 */
		public ModuleBuilder(Object... nonOptionalSockets) throws IllegalArgumentException {
			if(nonOptionalSockets.length%2 != 0) {
				throw new IllegalArgumentException("Invalid list of required socket");
			}
			List<String> nullSockets = new ArrayList<>();
			for(int i=0;i<nonOptionalSockets.length;i=i+2) {
				if(nonOptionalSockets[i+1] == null) {
					nullSockets.add(nonOptionalSockets[i].toString());
				}
			}
			if(nullSockets.size() > 0) {
				throw new IllegalArgumentException("Following non-optional sockets are null: " + nullSockets.stream().collect(Collectors.joining(", ")));
			}
		}
		
		/**
		 * <p>
		 * Set the banner to be displayed by the module to build.
		 * </p>
		 * 
		 * @param banner
		 *            The banner to set
		 * @return This builder
		 */
		public ModuleBuilder<T> banner(Banner banner) {
			this.banner = banner;
			return this;
		}
		
		/**
		 * <p>
		 * Build the module.
		 * </p>
		 * 
		 * </p>
		 * This method actually delegates the actual module creation to the
		 * {@link #doBuild()} method and sets its banner when specified.
		 * </p>
		 * 
		 * @return A new module instance
		 */
		public final T build() {
			T thisModule = this.doBuild();
			thisModule.setBanner(this.banner);
			return thisModule;
		}
		
		/**
		 * <p>
		 * This method should be implemented by concrete implementation to return the
		 * actual module instance.
		 * </p>
		 * 
		 * @return A new module instance
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
	 * @param <T> The actual type of the bean.
	 * 
	 * @author jkuhn
	 * @since 1.0
	 * @see BeanBuilder
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
		 * Create a bean with the specified name.
		 * </p>
		 * 
		 * @param name
		 *            The bean name
		 */
		protected Bean(String name) {
			this.name = name;
		}
		
		/**
		 * Create the underlying bean instance
		 */
		public abstract void create();

		/**
		 * Destroy the underlying instance.
		 */
		public abstract void destroy();
	}
	
	/**
	 * <p>
	 * A BeanBuilder is used within a module class to create {@link Bean} instances.
	 * </p>
	 * 
	 * <p>
	 * A {@link Bean} instance is built from a {@link Supplier} which is used to
	 * defer the actual instantiation of the bean. Post construction and destroy
	 * operations are invoked once the bean instance has been created (after
	 * dependency injection) and before its destruction respectively.
	 * </p>
	 * 
	 * <p>
	 * A BeanBuilder is always created for a specific module instance.
	 * </p>
	 * 
	 * <pre>
	 * {@code
	 *     this.bean = BeanBuilder
	 *         .singleton("bean", () -> {
	 *              BeanA beanA = new BeanA(serviceSocket.get());
	 *              return beanA;
	 *          })
	 *          .init(BeanA::init)
	 *          .destroy(BeanA::destroy)
	 *          .build(this);
	 * }
	 * </pre>
	 * 
	 * @param <T> The actual type of the bean to build
	 * 
	 * @author jkuhn
	 * @since 1.0
	 * @see Bean
	 */
	protected interface BeanBuilder<T> {

		/**
		 * <p>
		 * Return a Singleton Bean Builder.
		 * </p>
		 * 
		 * <p>
		 * Singleton {@link Bean}s are useful when one single instance of a bean should
		 * be injected through the application.
		 * </p>
		 * 
		 * @param beanName
		 *            The bean name
		 * @param constructor
		 *            the bean instance supplier
		 * @return A singleton Bean Builder
		 */
		static <T> BeanBuilder<T> singleton(String beanName, Supplier<T> constructor) {
			return new SingletonBeanBuilder<T>(beanName, constructor);
		}
		
		/**
		 * <p>
		 * Return a Prototype Bean Builder.
		 * <p>
		 * 
		 * <p>
		 * Prototype {@link Bean}s are useful when distinct instances of a bean should
		 * be injected though the application.
		 * </p>
		 * 
		 * @param beanName
		 *            the bean name
		 * @param constructor
		 *            the bean instance supplier
		 * @return A prototype Bean Builder
		 */
		static <T> BeanBuilder<T> prototype(String beanName, Supplier<T> constructor) {
			return new PrototypeBeanBuilder<T>(beanName, constructor);
		}

		/**
		 * <p>
		 * Add a bean initialization operation.
		 * </p>
		 * 
		 * @param init
		 *            The bean initialization operation.
		 * @return This builder
		 */
		BeanBuilder<T> init(Consumer<T> init);
		
		/**
		 * <p>
		 * Add a bean destruction operation.
		 * </p>
		 * 
		 * @param init
		 *            The bean destruction operation.
		 * @return This builder
		 */
		BeanBuilder<T> destroy(Consumer<T> destroy);
		
		/**
		 * <p>
		 * Build the bean.
		 * </p>
		 * 
		 * @return A bean
		 */
		public Bean<T> build();
	}
	
}
