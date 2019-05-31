package io.winterframework.test.generics;

public interface Service<E extends Action> {
	
	public void process(E action);
}
