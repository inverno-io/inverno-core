package io.winterframework.test.explicitwire.moduleG;

import io.winterframework.core.annotation.Bean;

@Bean
public class BeanG {

	public Runnable runnable;
	
	public BeanG(Runnable runnable) {
		this.runnable = runnable;
	}
}
