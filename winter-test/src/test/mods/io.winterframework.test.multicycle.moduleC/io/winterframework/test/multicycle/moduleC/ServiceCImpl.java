package io.winterframework.test.multicycle.moduleC;

import io.winterframework.test.multicycle.moduleAPI.ServiceC;
import io.winterframework.test.multicycle.moduleAPI.ServiceD;
import io.winterframework.core.annotation.Bean;

@Bean
public class ServiceCImpl implements ServiceC {
	
	public ServiceD serviceD;
	
	public ServiceCImpl(ServiceD serviceD) {
		this.serviceD = serviceD;
	}
}
