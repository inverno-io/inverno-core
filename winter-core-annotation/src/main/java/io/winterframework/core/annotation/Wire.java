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
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * <p>
 * Used on a module to specify an explicit wire directive when multiple matches
 * are found during the auto-wiring process for instance.
 * </p>
 * 
 * <p>
 * The Winter compiler will fail it finds multiple beans matching a single
 * socket, the Wire annotation is then used on a the module to explicitly tell
 * the compiler which bean has to be injected in a particular socket.
 * </p>
 * 
 * <p>
 * A Wire annotation can also be used to explicitly specify which beans must be
 * injected in a multiple socket like list or arrays.
 * </p>
 * 
 * @author jkuhn
 * @since 1.0
 */
@Retention(RetentionPolicy.CLASS)
@Target({ ElementType.MODULE })
@Repeatable(Wires.class)
public @interface Wire {

	/**
	 * <p>
	 * Indicates the names of the beans to wire.
	 * </p>
	 * 
	 * </p>
	 * A bean's name can be fully qualified (eg. [MODULE_NAME]:[BEAN_NAME]) to refer
	 * to a bean provided by an external module or simple to refer to a bean inside
	 * the current module (eg. [BEAN_NAME]).
	 * </p>
	 */
	String[] beans();

	/**
	 * <p>
	 * Indicates the socket where to inject the beans.
	 * </p>
	 * 
	 * <p>
	 * A socket name can be of three forms evaluated in this order:
	 * </p>
	 * 
	 * <ul>
	 * <li><em>[MODULE_NAME]:[BEAN_NAME]:[SOCKET_NAME]</em></li>
	 * <li><em>[BEAN_NAME]:[SOCKET_NAME]</em> where the module is implicitly the
	 * current module</li>
	 * <li><em>[MODULE_NAME]:[SOCKET_NAME]</em> to refer to a module socket.</li>
	 * </ul>
	 * 
	 * <p>
	 * Hopefully the Winter compiler doesn't let you define a bean with the same
	 * name as a required module which prevents conflicts between the last two
	 * identifiers.
	 * </p>
	 */
	String into();

}
