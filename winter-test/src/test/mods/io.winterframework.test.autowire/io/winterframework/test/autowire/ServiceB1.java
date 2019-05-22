package io.winterframework.test.autowire;

import io.winterframework.core.annotation.Bean;

@Bean
public class ServiceB1 implements ServiceB {
	
	public BeanD beanD;
	
	public ServiceB1(BeanD beanD) {
		this.beanD = beanD;
	}
}
