package io.winterframework.test.cycle;

import io.winterframework.core.annotation.Bean;

@Bean
public class BeanB {

	public BeanC beanC;
	
	public BeanB(BeanC beanC) {
		this.beanC = beanC;
	}
}
