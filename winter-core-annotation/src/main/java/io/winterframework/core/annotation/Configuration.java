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

import io.winterframework.core.annotation.Bean.Strategy;
import io.winterframework.core.annotation.Bean.Visibility;

/**
 * <p>
 * Used in conjunction with {@link Bean} on an interface to indicate a
 * configuration bean.
 * </p>
 * 
 * <p>
 * A configuration bean should be created when there's a need to provide
 * configuration data in a module. Configuration properties are declared as
 * non-void no-argument methods in an interface. Default values can be specified
 * in default methods. A configuration bean must be defined as a
 * {@link Visibility#PUBLIC} {@link Strategy#SINGLETON} bean.
 * <p>
 * 
 * <p>
 * It behaves like an optional socket bean with two peculiarities: although it
 * is an optional socket bean, an instance is always provided inside the module
 * using a default implementation and then an instance can (this is not
 * mandatory) be provided from a property of a configuration bean defined in an
 * enclosing module. This makes it very convenient to "chain" components modules
 * configurations in a single enclosing configuration.
 * </p>
 * 
 * <p>
 * Convenient methods are exposed on the module's builder class to easily
 * provide configuration properties to a module. Assuming we define a
 * configuration bean named "config" in a module with two properties "property1"
 * and "property2", we can configure a module instance as follows:
 * </p>
 * 
 * <pre>
 * SomeModule someModule = new SomeModule.Builder()
 *     .setConfig(configConfigurator -> configConfigurator
 *         .property1("someValue")
 *         .property2(42)
 *     ).build();
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
