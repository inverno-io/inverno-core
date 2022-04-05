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
package io.inverno.core.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * <p>
 * Indicates that a module is a Inverno module that must be processed during compilation in order to generate a module class.
 * </p>
 *
 * <p>
 * By default, the class is generated in a package named after the module's name and the class name is the last part of the module's name starting with a capital letter. For instance, class
 * <code>com.example.foo.Foo</code> is generated for module <code>com.example.foo</code>. In order for the module to be usable its package must be exported in the Java module.
 * </p>
 *
 * <p>
 * In case of name conflict, it is possible to explicitly specify the name of the generated class.
 * </p>
 *
 * <p>
 * By default, any Inverno module required by another Inverno module will be imported in that module which means that it will be instantiated in the module and its public beans made available for
 * dependency injection. As a consequence, an enclosing bean must provide all the beans required by the modules it imports otherwise it won't be able to instantiate them. You can use includes and/or
 * excludes values to control that behavior.
 * </p>
 *
 * @author <a href="mailto:jeremy.kuhn@inverno.io">Jeremy Kuhn</a>
 * @since 1.0
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.MODULE })
public @interface Module {

	/**
	 * <p>
	 * Indicates the name of the generated module class, defaults to the name of the module.
	 * </p>
	 *
	 * @return The module's class name
	 */
	String className() default "";

	/**
	 * <p>
	 * List required Inverno modules that must be included in the generated module class by the Inverno compiler, if none are specified include all.
	 * </p>
	 *
	 * @return A list of modules to include
	 */
	String[] includes() default {};

	/**
	 * <p>
	 * List required Inverno modules that must be excluded from the generated module class by the Inverno compiler.
	 * </p>
	 *
	 * @return A list of modules to exclude
	 */
	String[] excludes() default {};
	
	/**
	 * <p>
	 * Indicates the package in the module where to place source files generated by the Inverno compiler.
	 * </p>
	 *
	 * <p>
	 * Note that {@link Module#className()} takes precedence in the generation of the module class.
	 * </p>
	 *
	 * @return The package where to place generated source files.
	 */
	String sourcePackage() default "";
}
