package io.winterframework.test.multicycle.moduleD;

import io.winterframework.test.multicycle.moduleAPI.ServiceE;
import io.winterframework.core.annotation.Bean;

@Bean
public class BeanA {

	public ServiceE serviceE;
	
	public BeanA(ServiceE serviceE) {
		this.serviceE = serviceE;
	}
}
