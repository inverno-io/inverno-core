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
package io.winterframework.core.compiler.spi;

import javax.lang.model.type.TypeMirror;

/**
 * <p>
 * A configuration info holds the data required to process a configuration bean
 * in a module.
 * </p>
 * 
 * @author jkuhn
 *
 */
public interface ConfigurationInfo extends Info {

	/**
	 * <p>
	 * Returns a bean qualified name.
	 * </p>
	 * 
	 * @return a bean qualified name
	 */
	BeanQualifiedName getQualifiedName();
	
	/**
	 * <p>
	 * Returns the underlying type of the bean.
	 * </p>
	 * 
	 * @return a type
	 */
	TypeMirror getType();
	
	/**
	 * <p>
	 * Returns all the configuration properties defined in the configuration bean.
	 * </p>
	 * 
	 * @return an array of properties 
	 */
	ConfigurationPropertyInfo[] getProperties();
	
	/**
	 * <p>
	 * Returns the nested configuration properties defined in the configuration bean.
	 * </p>
	 * 
	 * @return an array of nested properties 
	 */
	NestedConfigurationPropertyInfo[] getNestedConfigurationProperties();
	
	/**
	 * <p>
	 * Returns the simple configuration properties defined in the configuration bean.
	 * </p>
	 * 
	 * @return an array of simple properties 
	 */
	NestedConfigurationPropertyInfo[] getSimpleProperties();
	
	/**
	 * <p>
	 * Returns the configuration socket bean representing a configuration bean in a module.
	 * </p>
	 * 
	 * @return A configuration socket
	 */
	ConfigurationSocketBeanInfo getSocket();
}
