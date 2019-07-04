package io.winterframework.test.multicycle.moduleE;

import io.winterframework.test.multicycle.moduleAPI.ServiceE;
import io.winterframework.test.multicycle.moduleAPI.ServiceF;

import io.winterframework.core.annotation.Bean;

@Bean
public class ServiceEImpl implements ServiceE {
	
	public ServiceF serviceF;
	
	public ServiceEImpl(ServiceF serviceF) {
		this.serviceF = serviceF;
	}
}
