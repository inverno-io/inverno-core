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
package io.inverno.core.compiler;

import javax.lang.model.type.TypeMirror;

/**
 * <p>
 * Exception thrown when a type is missing or could not be found.
 * </p>
 * 
 * <p>
 * This can typically indicates compilation errors on a class referenced in a
 * class processed by the Inverno compiler.
 * </p>
 * 
 * @author <a href="mailto:jeremy.kuhn@inverno.io">Jeremy Kuhn</a>
 *
 */
public class TypeErrorException extends Exception {

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
