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
package io.inverno.core.compiler.socket;

/**
 * <p>
 * Thrown when compilation errors have been reported on a socket bean excluding the bean from corresponding compiled module info.
 * </p>
 * 
 * @author <a href="mailto:jeremy.kuhn@inverno.io">Jeremy Kuhn</a>
 *
 */
public class SocketCompilationException extends Exception {

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
