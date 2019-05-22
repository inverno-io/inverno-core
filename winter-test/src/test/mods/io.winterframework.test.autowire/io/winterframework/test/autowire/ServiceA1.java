package io.winterframework.test.autowire;

import io.winterframework.core.annotation.Bean;

@Bean
public class ServiceA1 implements ServiceA {

	public BeanC beanC;
	
	public ServiceA1(BeanC beanC) {
		this.beanC = beanC;
	}
}
