package io.winterframework.test.generics;

public interface CustomService<E extends Action> extends Service<E> {
	
	public void process(E action, String argument);
}
