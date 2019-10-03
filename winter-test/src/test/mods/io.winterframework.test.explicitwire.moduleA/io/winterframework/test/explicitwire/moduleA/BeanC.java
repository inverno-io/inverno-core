package io.winterframework.test.explicitwire.moduleA;

import java.util.List;

import io.winterframework.core.annotation.Bean;

@Bean
public class BeanC {

	public List<Service> services;
	
	public BeanC(List<Service> services) {
		this.services = services;
	}
}
