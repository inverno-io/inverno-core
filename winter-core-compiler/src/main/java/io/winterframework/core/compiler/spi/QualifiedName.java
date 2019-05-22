/**
 * 
 */
package io.winterframework.core.compiler.spi;

/**
 * @author jkuhn
 *
 */
public abstract class QualifiedName {

	protected static final String SEPARATOR = ":";
	
	private String value;
	
	protected QualifiedName(String value) {
		this.value = value;
	}
	
	protected void validateQualifiedNamePart(String qnamePart) throws QualifiedNameFormatException {
		boolean start = true;
		for (String identifier : qnamePart.split("\\.")) {
			for (char c : identifier.toCharArray()) {
				if(start) {
					if(!Character.isJavaIdentifierStart(c)) {
						throw new QualifiedNameFormatException("QName part must be a valid Java identifier: " + qnamePart);
					}
					start = false;
				}
				else {
					if(!Character.isJavaIdentifierPart(c)) {
						throw new QualifiedNameFormatException("QName part must be a valid Java identifier: " + qnamePart);
					}
				}
			}
		}
	}
	
	public abstract String getSimpleValue();
	
	public String getValue() {
		return this.value;
	}
	
	public String normalize() {
		String result = this.getSimpleValue();
		int c = -1;
		while( (c = result.indexOf('.')) > 0 ) {
			result = result.substring(0, c) + Character.toUpperCase(result.charAt(c+1)) + result.substring(c+2);
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
