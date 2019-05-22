package io.winterframework.core.compiler.spi;

public interface Info {

	QualifiedName getQualifiedName();
	
	boolean hasError();
	
	boolean hasWarning();
	
	void error(String message);
	
	public void warning(String message);
}
