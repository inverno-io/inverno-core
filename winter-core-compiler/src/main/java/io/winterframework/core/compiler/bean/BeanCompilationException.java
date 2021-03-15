/*
 * Copyright 2019 Jeremy KUHN
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
package io.winterframework.core.compiler.bean;

/**
 * <p>
 * Exception thrown when a bean could not be process properly.
 * </p>
 * 
 * @author <a href="mailto:jeremy.kuhn@winterframework.io">Jeremy Kuhn</a>
 *
 */
public class BeanCompilationException extends Exception {

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
