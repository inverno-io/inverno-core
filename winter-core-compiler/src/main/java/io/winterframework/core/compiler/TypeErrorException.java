/**
 * 
 */
package io.winterframework.core.compiler;

import javax.lang.model.type.TypeMirror;

/**
 * @author jkuhn
 *
 */
public class TypeErrorException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 8298378122834303692L;
	
	private TypeMirror type;
	
	public TypeErrorException(TypeMirror type) {
		this.type = type;
	}

	public TypeErrorException(TypeMirror type, String message) {
		super(message);
		this.type = type;
	}

	public TypeErrorException(TypeMirror type, Throwable cause) {
		super(cause);
		this.type = type;
	}

	public TypeErrorException(TypeMirror type, String message, Throwable cause) {
		super(message, cause);
		this.type = type;
	}

	public TypeErrorException(TypeMirror type, String message, Throwable cause, boolean enableSuppression,
			boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
		this.type = type;
	}

	public TypeMirror getType() {
		return this.type;
	}
}
