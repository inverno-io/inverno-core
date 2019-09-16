package io.winterframework.test.selector.moduleD;

import io.winterframework.core.annotation.Bean;

import java.util.List;

import io.winterframework.test.selector.moduleB.BeanB;

@Bean
public class BeanD {

	public BeanB beanB;
	
	public BeanD(BeanB beanB) {
		this.beanB = beanB;
	}
}
