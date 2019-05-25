package io.winterframework.test.selfwire;

import io.winterframework.core.annotation.Bean;

@Bean
public class ServiceC implements Service2 {

	public Service2 service;
	
	public ServiceC(Service2 service) {
		this.service = service;
	}
	
	public String execute() {
		return "serviceC";
	}
}
