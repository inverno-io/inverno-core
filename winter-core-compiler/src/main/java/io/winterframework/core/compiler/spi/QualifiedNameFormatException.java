/**
 * 
 */
package io.winterframework.core.compiler.spi;

/**
 * @author jkuhn
 *
 */
public class QualifiedNameFormatException extends IllegalArgumentException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 888383750681678589L;

	public QualifiedNameFormatException() {
		super();
	}

	public QualifiedNameFormatException(String message, Throwable cause) {
		super(message, cause);
	}

	public QualifiedNameFormatException(String s) {
		super(s);
	}

	public QualifiedNameFormatException(Throwable cause) {
		super(cause);
	}

}
