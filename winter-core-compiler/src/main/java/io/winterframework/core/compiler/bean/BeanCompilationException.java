/**
 * 
 */
package io.winterframework.core.compiler.bean;

/**
 * @author jkuhn
 *
 */
public class BeanCompilationException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = -7631205833934260067L;

	public BeanCompilationException() {
	}

	public BeanCompilationException(String message) {
		super(message);
	}

	public BeanCompilationException(Throwable cause) {
		super(cause);
	}

	public BeanCompilationException(String message, Throwable cause) {
		super(message, cause);
	}

	public BeanCompilationException(String message, Throwable cause, boolean enableSuppression,
			boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}
}
