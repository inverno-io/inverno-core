package io.winterframework.test.multi.moduleC;

import io.winterframework.core.annotation.Bean;
import io.winterframework.test.multi.moduleA.BeanA;
import io.winterframework.test.multi.moduleB.BeanB;

@Bean
public class BeanC {
	
	public BeanA beanA;
	
	public BeanB beanB;

	public BeanC(BeanA beanA, BeanB beanB) {
		this.beanA = beanA;
		this.beanB = beanB;
	}
}
