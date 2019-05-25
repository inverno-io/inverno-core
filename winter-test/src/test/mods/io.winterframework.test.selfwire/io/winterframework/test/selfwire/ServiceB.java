package io.winterframework.test.selfwire;

import io.winterframework.core.annotation.Bean;

@Bean
public class ServiceB implements Service {

	public String execute() {
		return "serviceB";
	}
}
