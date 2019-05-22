package io.winterframework.test.cycle;

import io.winterframework.core.annotation.Bean;

@Bean
public class BeanA {

	public BeanB beanB;
	
	public BeanA(BeanB beanB) {
		this.beanB = beanB;
	}
}
