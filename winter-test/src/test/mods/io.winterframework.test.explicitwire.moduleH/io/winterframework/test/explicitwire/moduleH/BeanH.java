package io.winterframework.test.explicitwire.moduleH;

import io.winterframework.core.annotation.Bean;

@Bean
public class BeanH {

	public Runnable[] runnables;
	
	public BeanH(Runnable[] runnables) {
		this.runnables = runnables;
	}
}
