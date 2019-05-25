package io.winterframework.test.selfwire;

import io.winterframework.core.annotation.Bean;

@Bean
public class ServiceA implements Service {

	public String execute() {
		return "serviceA";
	}
}
