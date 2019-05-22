package io.winterframework.test.autowire;

import io.winterframework.core.annotation.Bean;

@Bean
public class ServiceA2 implements ServiceA {
	
	public BeanC beanC;
	
	public ServiceA2(BeanC beanC) {
		this.beanC = beanC;
	}
}
