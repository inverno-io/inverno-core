package io.winterframework.test.multi.moduleF;

import io.winterframework.core.annotation.Bean;

@Bean
public class BeanF {
	
	public Runnable beanA;
	
	public BeanF(Runnable beanA) {
		this.beanA = beanA;
	}
}
