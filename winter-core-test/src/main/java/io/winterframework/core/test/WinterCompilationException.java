/**
 * 
 */
package io.winterframework.core.test;

import java.util.List;

import javax.tools.Diagnostic;
import javax.tools.JavaFileObject;

/**
 * @author jkuhn
 *
 */
public class WinterCompilationException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = -6458997093664225512L;
	
	private List<Diagnostic<? extends JavaFileObject>> diagnotics;
	
	/**
	 * 
	 */
	public WinterCompilationException(List<Diagnostic<? extends JavaFileObject>> diagnotics) {
		this.diagnotics = diagnotics;
	}
	
	public List<Diagnostic<? extends JavaFileObject>> getDiagnotics() {
		return diagnotics;
	}
}
