package io.winterframework.test.multicycle.moduleF;

import io.winterframework.test.multicycle.moduleAPI.ServiceC;
import io.winterframework.test.multicycle.moduleAPI.ServiceF;

import io.winterframework.core.annotation.Bean;

@Bean
public class ServiceFImpl implements ServiceF {
	
	public ServiceC serviceC;
	
	public ServiceFImpl(ServiceC serviceC) {
		this.serviceC = serviceC;
	}
}
