/**
 * 
 */
package io.winterframework.core.compiler.socket;

/**
 * @author jkuhn
 *
 */
public class SocketCompilationException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 4086796549827905758L;

	public SocketCompilationException() {
	}

	public SocketCompilationException(String message) {
		super(message);
	}

	public SocketCompilationException(Throwable cause) {
		super(cause);
	}

	public SocketCompilationException(String message, Throwable cause) {
		super(message, cause);
	}

	public SocketCompilationException(String message, Throwable cause, boolean enableSuppression,
			boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

}
