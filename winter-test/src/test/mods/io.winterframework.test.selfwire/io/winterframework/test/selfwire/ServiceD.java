package io.winterframework.test.selfwire;

import io.winterframework.core.annotation.Bean;

@Bean
public class ServiceD implements Service2 {

	public String execute() {
		return "serviceD";
	}
}
