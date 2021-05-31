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
package io.inverno.test;

import java.util.List;

import javax.tools.Diagnostic;
import javax.tools.JavaFileObject;

/**
 * @author <a href="mailto:jeremy.kuhn@inverno.io">Jeremy Kuhn</a>
 *
 */
public class InvernoCompilationException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = -6458997093664225512L;
	
	private List<Diagnostic<? extends JavaFileObject>> diagnostics;
	
	/**
	 * 
	 */
	public InvernoCompilationException(List<Diagnostic<? extends JavaFileObject>> diagnostics) {
		this.diagnostics = diagnostics;
	}
	
	public List<Diagnostic<? extends JavaFileObject>> getDiagnostics() {
		return diagnostics;
	}
}
