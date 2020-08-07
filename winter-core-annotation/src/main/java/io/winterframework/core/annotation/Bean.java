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
package io.winterframework.core.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.function.Supplier;

/**
 * <p>
 * Indicates that an annotated class or interface is a bean. Inside a module, a
 * bean represents one or more instance that can be wired to other bean
 * instances visible to this module.
 * </p>
 * 
 * <p>
 * A bean is fully identified by its name (which defaults to the name of the
 * class) and the name of the module exposing the bean (eg.
 * [MODULE_NAME]:[BEAN_NAME]). We can differentiate four kinds of beans: module
 * bean, wrapper bean, socket bean and configuration bean.
 * </p>
 * 
 * <p>
 * A module bean is automatically instantiated and wired. Its dependencies must
 * be defined in injection points or sockets which can be either the constructor
 * for required dependencies or setter methods for optional dependencies. By
 * convention, any setter method is considered as a socket which may lead to
 * ambiguities. In that case a {@link BeanSocket} annotation can be used to
 * specify explicit bean sockets.
 * </p>
 * 
 * <pre>
 *     &#64;Bean
 *     public class ModuleBean implements SomeService {
 *         
 *         public ModuleBean(RequiredDependency requiredDependency) {
 *             ...
 *         }
 *         
 *         public void setOptionalDependency(OptionalDependency optionalDependency) {
 *             ...
 *         }
 *         
 *         &#64;Init
 *         public void init() {
 *             ...
 *         }
 *         
 *         &#64;Destroy
 *         public void destroy() {
 *             ...
 *         }
 *     }
 * </pre>
 * 
 * <p>
 * A wrapper bean is used to expose legacy code that can't be instrumented. A
 * wrapper bean must implement {@link Supplier} and be annotated with
 * {@link Wrapper}.
 * </p>
 * 
 * <pre>
 *     &#64;Bean
 *     &#64;Wrapper
 *     public class WrapperBean implements Supplier&lt;SomeService&gt; {
 *         
 *         private WeakReference<SomeService> instance;
 *         
 *         public WrapperBean(RequiredDependency requiredDependency) {
 *             // Instantiate the wrapped instance
 *             this.instance = new WeakReference<>(...)
 *         }
 *         
 *         public void setOptionalDependency(OptionalDependency optionalDependency) {
 *             // Set optional dependency on the instance
 *             this.instance.set...
 *         }
 *         
 *         public SomeService get() {
 *             return this.instance.get();
 *         }
 *         
 *         &#64;Init
 *         public void init() {
 *             // Init the instance
 *             this.instance.get().init();
 *         }
 *         
 *         &#64;Destroy
 *         public void destroy() {
 *             // Destroy the instance
 *             this.instance.get().destroy();
 *         }
 *     }
 * </pre>
 * 
 * <p>
 * A socket bean is a particular type of bean which is used to declare a module
 * dependency that is a bean required or desirable by the beans in the module to
 * operate properly. As for bean socket, it should be seen as an injection point
 * at module level to inject an external bean into the module (hence the
 * "socket" designation). From a dependency injection perspective, inside the
 * module, a socket bean is considered just like any other bean and is
 * automatically or explicitly injected in beans visible to the module. A socket
 * bean must be an interface annotated with {@link Bean} with a
 * {@link Visibility#PUBLIC} visibility and extends {@link Supplier}.
 * </p>
 * 
 * <pre>
 *     &#64;Bean
 *     public interface SocketBean implements Supplier&lt;SomeService&gt; {
 *     }
 * </pre>
 * 
 * <p>
 * A configuration bean is a particular type of bean used to create specific
 * socket beans providing configuration data. It supports specific features that
 * makes it very convenient to use when one need to provide configuration data
 * to a module. A configuration bean must be an interface annotated with
 * {@link Bean} and {@link Configuration} with a {@link Visibility#PUBLIC}
 * visibility. Configuration properties are specified in non-void no-argument
 * methods and default values can be specified in default methods.
 * </p>
 * 
 * <pre>
 *     &#64;Bean
 *     &#64;Configuration
 *     public interface ConfigurationBean {
 *         
 *         String paramerter1();
 *         
 *         default int parameter2() {
 *             return 42;
 *         }
 *     }
 * </pre>
 * 
 * @author jkuhn
 * @since 1.0
 * 
 * @see BeanSocket
 * @see Wrapper
 * @see Configuration
 */
@Retention(RetentionPolicy.CLASS)
@Target({ ElementType.TYPE })
public @interface Bean {

	/**
	 * <p>
	 * Indicates a name identifying the bean in the module, defaults to the name of
	 * the class.
	 * </p>
	 * 
	 * @return A name
	 */
	String name() default "";

	/**
	 * Indicates the visibility of a bean in a module.
	 * 
	 * @author jkuhn
	 * @since 1.0
	 */
	public static enum Visibility {
		/**
		 * A private bean is only accessible inside the module.
		 */
		PRIVATE,
		/**
		 * A public bean is accessible inside the module and in enclosing modules.
		 */
		PUBLIC;
	}

	/**
	 * <p>
	 * Indicates the visibility of the bean in the module.
	 * </p>
	 * 
	 * <p>
	 * Usually, you're most likely to create public beans exposed to other modules.
	 * Private bean are provided as a convenience to let the framework instantiate
	 * and wire internal beans instead of doing it explicitly.
	 * </p>
	 * 
	 * @return The bean's visibility
	 */
	Visibility visibility() default Visibility.PUBLIC;

	/**
	 * <p>
	 * Indicates the strategy to use to instantiate the bean.
	 * </p>
	 * 
	 * <p>
	 * A {@link Strategy#SINGLETON} bean is only instantiated once in a module and
	 * this single instance is returned when requested. As a result any dependent
	 * bean share the same instance. This is the default behavior when no scope is
	 * specified.
	 * </p>
	 * 
	 * <p>
	 * A {@link Strategy#PROTOTYPE} bean is instantiated each time it is requested
	 * which means every dependent beans receive distinct instances.
	 * </p>
	 * 
	 * <p>
	 * Note that this attribute is irrelevant and therefore ignored when specified
	 * on a socket bean
	 * </p>
	 * 
	 * @author jkuhn
	 * @since 1.0
	 */
	public static enum Strategy {
		/**
		 * Singleton strategy results in one single instance being created.
		 */
		SINGLETON,
		/**
		 * Prototype strategy results in multiple instance being created when requested.
		 */
		PROTOTYPE
	}

	/**
	 * The bean strategy which defaults to {@link Strategy#SINGLETON}.
	 * 
	 * @return The bean's strategy
	 */
	Strategy strategy() default Strategy.SINGLETON;
}
