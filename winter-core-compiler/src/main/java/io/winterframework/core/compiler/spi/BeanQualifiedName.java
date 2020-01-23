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
package io.winterframework.core.compiler.spi;

/**
 * <p>
 * A qualified name identifying a bean.
 * </p>
 * 
 * <p>
 * The raw representation of a bean qualified name is of the form
 * <code>ModuleQualifiedName():&lt;beanName&gt;</code> where
 * <code>&lt;beanName&gt;</code> is a valid Java name (eg.
 * <code>com.example.myModule:myBean</code>).
 * </p>
 * 
 * <p>
 * The name of a bean is tightly linked to the name of the corresponding Java
 * class.
 * </p>
 * 
 * @author jkuhn
 *
 */
public class BeanQualifiedName extends QualifiedName {

	/**
	 * <p>
	 * The module qualified name defining the bean.
	 * </p>
	 */
	private ModuleQualifiedName moduleQName;
	
	/**
	 * <p>
	 * The name of the bean.
	 * </p>
	 */
	private String beanName;
	
	/**
	 * <p>
	 * Creates a bean qualified name from the specified module qualified name and
	 * bean name.
	 * </p>
	 * 
	 * @param moduleQName The qualified name of the module defining the bean.
	 * @param beanName    The name of the bean.
	 * @throws QualifiedNameFormatException If the specified bean name is invalid.
	 */
	public BeanQualifiedName(ModuleQualifiedName moduleQName, String beanName) throws QualifiedNameFormatException {
		super(moduleQName.getValue() + SEPARATOR + beanName);
		
		this.moduleQName = moduleQName;
		
		this.validateQualifiedNamePart(beanName);
		this.beanName = beanName;
	}
	
	@Override
	public String getSimpleValue() {
		return this.getBeanName();
	}
	
	/**
	 * <p>
	 * Returns the qualified name of the module defining the bean.
	 * </p>
	 * 
	 * @return A module qualified name.
	 */
	public ModuleQualifiedName getModuleQName() {
		return this.moduleQName;
	}
	
	/**
	 * <p>
	 * Returns the name of the bean.
	 * </p>
	 * 
	 * @return The bean name.
	 */
	public String getBeanName() {
		return this.beanName;
	}
	
	/**
	 * <p>
	 * Creates a bean qualified name from the specified raw value of the form
	 * <code>ModuleQualifiedName():&lt;beanName&gt;</code> where
	 * <code>&lt;beanName&gt;</code> is a valid Java name.
	 * </p>
	 * 
	 * @param qname A raw qualified name.
	 * @return A bean qualified name.
	 * @throws QualifiedNameFormatException If the specified value is not a bean
	 *                                      qualified name.
	 */
	public static BeanQualifiedName valueOf(String qname) throws QualifiedNameFormatException {
		int lastSeparatorIndex = qname.lastIndexOf(SEPARATOR);
		if(lastSeparatorIndex == -1) {
			throw new QualifiedNameFormatException("Invalid qname " + qname + ", was expecting: ModuleQualifiedName():<beanName>");
		}
		return new BeanQualifiedName(ModuleQualifiedName.valueOf(qname.substring(0, lastSeparatorIndex)), qname.substring(lastSeparatorIndex+1));
	}
}
