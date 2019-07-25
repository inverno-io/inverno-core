/**
 * 
 */
package io.winterframework.core;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
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
 * The following describes the module startup steps:
 * </p>
 * <ol>
 * <li>Write the banner to the log output if the module is a top module.</li>
 * <li>Start the imported modules.</li>
 * <li>Create the module beans.</li>
 * </ol>
 * 
 * <p>
 * The following describes the module destroy steps:
 * </p>
 * <ol>
 * <li>Stop the imported modules.</li>
 * <li>Destroy the module beans.</li>
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
	 * The list of modules imported by the module.
	 */
	private List<Module> modules;
	
	/**
	 * The list of beans in the module.
	 */
	private List<Bean<?>> beans;
	
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
		this.modules = new ArrayList<>();
	}
	
	/**
	 * <p>
	 * Set the module banner to be written to the log output if the module is a top
	 * module.
	 * </p>
	 * 
	 * @param banner
	 *            The banner to set
	 */
	void setBanner(Banner banner) {
		this.banner = banner;
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
	 * Determine whether the banner should be displayed, only top module should
	 * display the banner.
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
	 * @param moduleCreator The module creator to use to create the module
	 * @return The registered module
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
	 * Start the module.
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
	}

	/**
	 * <p>
	 * Stop the module.
	 * </p>
	 */
	public void stop() {
		long t0 = System.nanoTime();
		this.logger.info("Stopping Module " + this.name + "...");
		this.modules.stream().forEach(module -> module.stop());
		this.beans.stream().forEach(bean -> bean.destroy());
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
		 * @return A list of beans
		 */
		public List<E> toList() {
			return this.aggregate.stream().filter(Objects::nonNull).collect(Collectors.toList());
		}
		
		/**
		 * <p>
		 * Filter null bean and return a set representation of the aggregate.
		 * </p>
		 * @return A set of beans
		 */
		public Set<E> toSet() {
			return this.aggregate.stream().filter(Objects::nonNull).collect(Collectors.toSet());
		}
		
		/**
		 * <p>
		 * Filter null bean and return an array representation of the aggregate.
		 * </p>
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
	 * modules into the current module. A linker shall never be used directly.
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
		 * <p>Create a new Module linker with the specified socket map.</p>
		 * @param sockets The socket map
		 */
		public ModuleLinker(Map<String, Object> sockets) {
			this.sockets = sockets;
		}
		
		/**
		 * </p>Link the socket map in a new module instance and return that instance.</p>
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
	 * All module have to be built by a builder. A module should register the module
	 * it imports.
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
		 * <p>
		 * Check that non-optional sockets are not null.
		 * </p>
		 * 
		 * @param nonOptionalSockets
		 *            An even list of non-optional sockets pairs (name, value) required to build the module
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
		 * Set the banner for the module to build.
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
		 * {@link #doBuild()} method and set its banner when specified.
		 * </p>
		 * 
		 * @return A new module instance
		 */
		public final T build() {
			T thisModule = this.doBuild();
			if(this.banner == null) {
				this.banner = new DefaultBanner();
			}
			thisModule.setBanner(this.banner);
			return thisModule;
		}
		
		/**
		 * <p>
		 * This method should be implemented by concrete module builder to return the
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
	 * perform dependency injection in order to defer the instantiation of actual
	 * beans at module startup. Dependency cycles, missing dependencies and other
	 * issues related to dependency injection should have been raised at compile
	 * time.
	 * </p>
	 * 
	 * @param <T>
	 *            The actual type of the bean.
	 * 
	 * @author jkuhn
	 * @since 1.0
	 * @see BeanBuilder
	 */
	protected static abstract class Bean<T> implements Supplier<T> {

		/**
		 * The module enclosing the bean.
		 */
		protected Module parent;

		/**
		 * The bean name.
		 */
		protected String name;

		/**
		 * <p>
		 * Create an abstract bean with the specified name.
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
	 * A BeanBuilder is used within a generated module class to create {@link Bean}
	 * instances.
	 * </p>
	 * 
	 * <p>
	 * A {@link Bean} instance is built from a {@link Supplier} which is used to
	 * defer the actual bean creation, {@link Consumer} post construction and
	 * destroy operations invoked after the actual bean creation and before its
	 * destruction respectively. It is always created for a specific module
	 * instance.
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
	 * @param <T>
	 *            The actual type of the bean to build
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
