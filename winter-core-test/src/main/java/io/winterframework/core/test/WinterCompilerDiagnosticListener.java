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
package io.winterframework.core.test;

import java.util.ArrayList;
import java.util.List;

import javax.tools.Diagnostic;
import javax.tools.DiagnosticListener;
import javax.tools.JavaFileObject;

/**
 * @author jkuhn
 *
 */
public class WinterCompilerDiagnosticListener implements DiagnosticListener<JavaFileObject> {

	private List<Diagnostic<? extends JavaFileObject>> diagnotics;
	
	public WinterCompilerDiagnosticListener() {
		this.diagnotics = new ArrayList<>();
	}
	
	/* (non-Javadoc)
	 * @see javax.tools.DiagnosticListener#report(javax.tools.Diagnostic)
	 */
	@Override
	public void report(Diagnostic<? extends JavaFileObject> diagnostic) {
		this.diagnotics.add(diagnostic);
	}
	
	public List<Diagnostic<? extends JavaFileObject>> getDiagnotics() {
		return diagnotics;
	}
}
