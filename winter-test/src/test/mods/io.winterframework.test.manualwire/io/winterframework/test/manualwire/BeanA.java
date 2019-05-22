package io.winterframework.test.manualwire;

import io.winterframework.core.annotation.Bean;

@Bean
public class BeanA {

	public Service service;
	
	public BeanA(Service service) {
		this.service = service;
	}
}
