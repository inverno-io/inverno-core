package io.winterframework.test.multicycle.moduleD;

import io.winterframework.test.multicycle.moduleAPI.ServiceD;
import io.winterframework.core.annotation.Bean;

@Bean
public class ServiceDImpl implements ServiceD {

	public BeanA beanA;
	
	public ServiceDImpl(BeanA beanA) {
		this.beanA = beanA;
	}
}
