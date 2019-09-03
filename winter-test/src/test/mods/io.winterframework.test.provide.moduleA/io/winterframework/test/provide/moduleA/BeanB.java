package io.winterframework.test.provide.moduleA;

import io.winterframework.core.annotation.Bean;

@Bean
public class BeanB {

	public Runnable runnable;
	
	public BeanB(Runnable runnable) {
		this.runnable = runnable;
	}
}
