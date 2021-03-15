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
 * Thrown by a {@link QualifiedName} implementation to indicate a bad format.
 * </p>
 * 
 * @author <a href="mailto:jeremy.kuhn@winterframework.io">Jeremy Kuhn</a>
 *
 */
public class QualifiedNameFormatException extends IllegalArgumentException {

	private static final long serialVersionUID = 888383750681678589L;

	/**
	 * <p>
	 * Creates a qualified name format exception.
	 * </p>
	 */
	public QualifiedNameFormatException() {
		super();
	}

	/**
	 * <p>
	 * Creates a qualified name format exception with the specified message.
	 * </p>
	 * 
	 * @param message the message
	 */
	public QualifiedNameFormatException(String message) {
		super(message);
	}

	/**
	 * <p>
	 * Creates a qualified name format exception with the specified cause.
	 * </p>
	 * 
	 * @param cause the cause
	 */
	public QualifiedNameFormatException(Throwable cause) {
		super(cause);
	}

	/**
	 * <p>
	 * Creates a qualified name format exception with the specified message and
	 * cause.
	 * </p>
	 * 
	 * @param message the message
	 * @param cause   the cause
	 */
	public QualifiedNameFormatException(String message, Throwable cause) {
		super(message, cause);
	}
}
