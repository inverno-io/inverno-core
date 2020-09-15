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
package io.winterframework.core.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * <p>
 * Used on an interface to indicate a configuration.
 * </p>
 * 
 * <p>
 * A configuration should be created when there's a need to provide
 * configuration data in a module. Configuration properties are declared as
 * non-void no-argument methods in an interface. Default values can be specified
 * in default methods.
 * <p>
 * 
 * <p>
 * For a given configuration, a module provides a configurator to easily create configuration instances.  
 * </p>
 * 
 * <pre>
 * Config config = SomeModule.ConfigConfigurator.create(configConfigurator -> configConfigurator.property1("someValue").property2(42));
 * </pre>
 * 
 * <p>
 * When used in conjunction with {@link Bean}, an optional socket bean is
 * provided with two peculiarities: although it is an optional socket bean, an
 * instance is always provided inside the module using the configuration default
 * implementation and then an instance can (this is not mandatory) be provided
 * from a property of a configuration bean defined in an enclosing module. This
 * makes it very convenient to "chain" components modules configurations in a
 * single enclosing configuration.
 * </p>
 * 
 * <p>
 * Convenient methods are also exposed on the module's builder class to easily
 * provide configuration properties to a module. Assuming we define a
 * configuration bean named "config" in a module with two properties "property1"
 * and "property2", we can configure a module instance as follows:
 * </p>
 * 
 * <pre>
 * SomeModule someModule = new SomeModule.Builder()
 *     .setConfig(configConfigurator -> configConfigurator.property1("someValue").property2(42))
 *     .build();
 * </pre>
 * 
 * 
 * @author jkuhn
 * @since 1.1
 * 
 * @see Bean
 */
@Retention(RetentionPolicy.CLASS)
@Target({ ElementType.TYPE })
@Bean
public @interface Configuration {

}
