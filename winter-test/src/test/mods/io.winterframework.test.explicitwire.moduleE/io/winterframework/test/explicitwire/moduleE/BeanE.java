package io.winterframework.test.explicitwire.moduleE;

import io.winterframework.core.annotation.Bean;

@Bean
public class BeanE {

	public Service[] services;
	
	public BeanE(Service[] services) {
		this.services = services;
	}
}
