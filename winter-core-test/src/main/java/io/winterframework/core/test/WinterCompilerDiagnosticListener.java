/**
 * 
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
