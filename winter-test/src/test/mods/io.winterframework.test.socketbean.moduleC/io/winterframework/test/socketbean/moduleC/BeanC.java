package io.winterframework.test.socketbean.moduleC;

import io.winterframework.core.annotation.Bean;

import io.winterframework.test.socketbean.moduleB.BeanB;

@Bean
public class BeanC {

	public BeanB beanB;
	
	public BeanC(BeanB beanB) {
		this.beanB = beanB;
	}
}
