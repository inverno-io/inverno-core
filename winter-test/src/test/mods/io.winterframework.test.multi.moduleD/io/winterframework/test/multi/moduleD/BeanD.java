package io.winterframework.test.multi.moduleD;

import io.winterframework.core.annotation.Bean;
import io.winterframework.test.multi.moduleE.BeanE;
import io.winterframework.test.multi.moduleF.BeanF;

@Bean
public class BeanD {
	
	public BeanE beanE;
	
	public BeanF beanF;

	public BeanD(BeanE beanE, BeanF beanF) {
		this.beanE = beanE;
		this.beanF = beanF;
	}
}
