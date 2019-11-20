package io.winterframework.test.explicitwire.moduleJ;

import io.winterframework.core.annotation.Bean;

@Bean
public class BeanJ {

	public Runnable runnable;
	
	public BeanJ(Runnable runnable) {
		this.runnable = runnable;
	}
}
