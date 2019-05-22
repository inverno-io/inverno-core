package io.winterframework.test.autowire;

import io.winterframework.core.annotation.Bean;
import io.winterframework.core.annotation.BeanSocket;

@Bean
public class BeanE {

	public BeanA beanA;

	public BeanB beanB;
	
	public BeanC beanC;
	
	public BeanD beanD;
	
	@BeanSocket
	public BeanE(BeanA beanA) {
		this.beanA = beanA;
	}
	
	public BeanE(BeanA beanA, BeanD beanD) {
		this.beanA = beanA;
		this.beanD = beanD;
	}
	
	@BeanSocket
	public void setBeanB(BeanB beanB) {
		this.beanB = beanB;
	}
	
	public void setBeanC(BeanC beanC) {
		this.beanC = beanC;
	}
}
