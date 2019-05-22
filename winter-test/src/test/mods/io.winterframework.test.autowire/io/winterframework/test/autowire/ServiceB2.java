package io.winterframework.test.autowire;

import io.winterframework.core.annotation.Bean;

@Bean
public class ServiceB2 implements ServiceB {
	
	public BeanD beanD;
	
	public ServiceB2(BeanD beanD) {
		this.beanD = beanD;
	}
}
