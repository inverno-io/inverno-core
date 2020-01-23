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
 * A qualified name identifying a bean socket.
 * </p>
 * 
 * <p>
 * The raw representation of a bean socket qualified name is of the form
 * <code>BeanQualifiedName():&lt;socketName&gt;</code> where
 * <code>&lt;socketName&gt;</code> is a valid Java name corresponding to the
 * name a constructor or setter method argument (eg.
 * <code>com.example.myModule:myBean:param</code>).
 * </p>
 * 
 * @author jkuhn
 *
 */
public class BeanSocketQualifiedName extends QualifiedName {

	/**
	 * <p>
	 * The bean qualified name defining the socket.
	 * </p>
	 */
	private BeanQualifiedName beanQName;

	/**
	 * <p>
	 * The name of the socket.
	 * </p>
	 */
	private String name;

	/**
	 * <p>
	 * Creates a bean socket qualified name from the specified bean qualified name
	 * and socket name.
	 * </p>
	 * 
	 * @param beanQName The qualified name of the bean defining the socket.
	 * @param name      The name of the socket.
	 * @throws QualifiedNameFormatException If the specified socket name is invalid.
	 */
	public BeanSocketQualifiedName(BeanQualifiedName beanQName, String name) throws QualifiedNameFormatException {
		super(beanQName.getValue() + SEPARATOR + name);

		this.beanQName = beanQName;
		this.name = name;

		this.validateQualifiedNamePart(this.name);
	}

	@Override
	public String getSimpleValue() {
		return this.getName();
	}

	/**
	 * <p>
	 * Returns the name of the bean defining the socket.
	 * </p>
	 * 
	 * @return A bean qualified name.
	 */
	public BeanQualifiedName getBeanQName() {
		return this.beanQName;
	}

	/**
	 * <p>
	 * Returns the name of the socket.
	 * </p>
	 * 
	 * @return The socket name.
	 */
	public String getName() {
		return this.name;
	}

	/**
	 * <p>
	 * Creates a bean socket qualified name from the specified raw value of the form
	 * <code>BeanQualifiedName():&lt;socketName&gt;</code> where
	 * <code>&lt;socketName&gt;</code> is a valid Java name.
	 * </p>
	 * 
	 * @param qname A raw qualified name.
	 * @return A bean socket qualified name.
	 * @throws QualifiedNameFormatException If the specified value is not a bean
	 *                                      socket qualified name.
	 */
	public static BeanSocketQualifiedName valueOf(String qname) throws QualifiedNameFormatException {
		int lastSeparatorIndex = qname.lastIndexOf(SEPARATOR);
		if (lastSeparatorIndex == -1) {
			throw new QualifiedNameFormatException("Invalid qname " + qname + ", was expecting: BeanQualifiedName():<socketName>");
		}
		return new BeanSocketQualifiedName(BeanQualifiedName.valueOf(qname.substring(0, lastSeparatorIndex)), qname.substring(lastSeparatorIndex + 1));
	}

	/**
	 * <p>
	 * Creates a bean socket qualified name from the specified module qualified name
	 * and the specified raw value of the form
	 * <code>&lt;beanName&gt;:&lt;socketName&gt;</code> where
	 * <code>&lt;beanName&gt;</code> and <code>&lt;socketName&gt;</code> is a valid
	 * Java name.
	 * </p>
	 * 
	 * @param moduleQName A module qualified name.
	 * @param qname       A raw qualified name.
	 * @return A bean socket qualified name.
	 * @throws QualifiedNameFormatException If the specified value is not a bean
	 *                                      socket qualified name.
	 */
	public static BeanSocketQualifiedName valueOf(ModuleQualifiedName moduleQName, String qname) throws QualifiedNameFormatException {
		String[] qnameParts = qname.split(SEPARATOR);
		if (qnameParts.length != 2) {
			throw new QualifiedNameFormatException("Invalid qname " + qname + ", was expecting: <beanName>:<socketName>");
		}
		return new BeanSocketQualifiedName(new BeanQualifiedName(moduleQName, qnameParts[0]), qnameParts[1]);
	}
}
