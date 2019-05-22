package io.winterframework.test.cycle;

import io.winterframework.core.annotation.Bean;

@Bean
public class BeanC {

	public BeanA beanA;
	
	public BeanC(BeanA beanA) {
		this.beanA = beanA;
	}
}
