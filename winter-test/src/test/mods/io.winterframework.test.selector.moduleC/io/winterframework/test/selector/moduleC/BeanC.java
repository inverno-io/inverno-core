package io.winterframework.test.selector.moduleC;

import io.winterframework.core.annotation.Bean;

import java.util.List;

import io.winterframework.test.selector.moduleB.BeanB;

@Bean
public class BeanC {

	public BeanB beanB;
	
	public BeanC(BeanB beanB) {
		this.beanB = beanB;
	}
}
