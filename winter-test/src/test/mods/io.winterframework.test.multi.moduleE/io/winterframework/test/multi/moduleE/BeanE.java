package io.winterframework.test.multi.moduleE;

import io.winterframework.core.annotation.Bean;

@Bean
public class BeanE {
	
	public Runnable beanA;
	
	public BeanE(Runnable beanA) {
		this.beanA = beanA;
	}
}
