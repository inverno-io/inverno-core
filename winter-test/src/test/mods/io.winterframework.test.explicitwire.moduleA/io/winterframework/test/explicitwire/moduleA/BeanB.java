package io.winterframework.test.explicitwire.moduleA;

import io.winterframework.core.annotation.Bean;

@Bean
public class BeanB {

	public Service service;
	
	public BeanB(Service service) {
		this.service = service;
	}
}
