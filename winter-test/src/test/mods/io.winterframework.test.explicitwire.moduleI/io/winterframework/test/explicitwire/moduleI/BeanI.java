package io.winterframework.test.explicitwire.moduleI;

import io.winterframework.core.annotation.Bean;

@Bean
public class BeanI {

	public Runnable[] runnables;
	
	public BeanI(Runnable[] runnables) {
		this.runnables = runnables;
	}
}
