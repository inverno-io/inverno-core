package io.winterframework.test.multi.moduleB;

import io.winterframework.core.annotation.Bean;
import io.winterframework.test.multi.moduleA.BeanA;

@Bean
public class BeanB {
	
	public BeanA beanA;

	public BeanB(BeanA beanA) {
		this.beanA = beanA;
	}
}
