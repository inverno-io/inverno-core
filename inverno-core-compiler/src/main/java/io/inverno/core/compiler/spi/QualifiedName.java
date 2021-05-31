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
package io.inverno.core.compiler.spi;

/**
 * <p>
 * Base qualified name class.
 * </p>
 * 
 * <p>
 * A qualified name is of the form <code>&lt;part&gt;(:&lt;part&gt;)+</code>
 * where <code>&lt;part&gt;</code> is a valid Java name of the form
 * <code>&lt;identifier&gt;(.&lt;identifier&gt;)+</code>.
 * </p>
 * 
 * @author <a href="mailto:jeremy.kuhn@inverno.io">Jeremy Kuhn</a>
 *
 */
public abstract class QualifiedName {

	/**
	 * <p>
	 * The qualified name part separator.
	 * </p>
	 */
	protected static final String SEPARATOR = ":";

	/**
	 * <p>
	 * The raw qualified name value.
	 * </p>
	 */
	private final String value;

	/**
	 * <p>
	 * Creates a qualified name with the specified raw value.
	 * </p>
	 * 
	 * @param value the raw value
	 */
	protected QualifiedName(String value) {
		this.value = value;
	}

	/**
	 * <p>
	 * Validates that the specified qualified name part is valid.
	 * </p>
	 * 
	 * <p>
	 * A Java name is a valid qualified name part.
	 * </p>
	 * 
	 * @param qnamePart the qualified name part to validate
	 * 
	 * @throws QualifiedNameFormatException if the specified qualified name part is
	 *                                      invalid
	 */
	protected void validateQualifiedNamePart(String qnamePart) throws QualifiedNameFormatException {
		boolean start = true;
		for (String identifier : qnamePart.split("\\.")) {
			for (char c : identifier.toCharArray()) {
				if (start) {
					if (!Character.isJavaIdentifierStart(c)) {
						throw new QualifiedNameFormatException(
								"QName part must be a valid Java identifier: " + qnamePart);
					}
					start = false;
				} else {
					if (!Character.isJavaIdentifierPart(c)) {
						throw new QualifiedNameFormatException(
								"QName part must be a valid Java identifier: " + qnamePart);
					}
				}
			}
		}
	}

	/**
	 * <p>
	 * Returns a simple representation of this qualified name.
	 * </p>
	 * 
	 * <p>
	 * Note that this representation is no longer "qualified".
	 * </p>
	 * 
	 * @return a simple representation
	 */
	public abstract String getSimpleValue();

	/**
	 * <p>
	 * Returns the qualified name raw value.
	 * </p>
	 * 
	 * @return the raw value
	 */
	public String getValue() {
		return this.value;
	}

	/**
	 * <p>
	 * Returns a normalized representation of the qualified name.
	 * </p>
	 * 
	 * <p>
	 * This methods basically converts the simple value to a camel case Java
	 * identifier by removing possible dots. As for the simple value the normalized
	 * representation is not "qualified".
	 * </p>
	 * 
	 * @return a normalized representation
	 */
	public String normalize() {
		String result = this.getSimpleValue();
		int c = -1;
		while ((c = result.indexOf('.')) > 0) {
			result = result.substring(0, c) + Character.toUpperCase(result.charAt(c + 1)) + result.substring(c + 2);
		}
		return result;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((value == null) ? 0 : value.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		QualifiedName other = (QualifiedName) obj;
		if (value == null) {
			if (other.value != null)
				return false;
		} else if (!value.equals(other.value))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return this.getValue();
	}
}
