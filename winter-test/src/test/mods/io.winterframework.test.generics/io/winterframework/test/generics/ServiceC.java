package io.winterframework.test.generics;

import io.winterframework.core.annotation.Bean;

@Bean
public class ServiceC implements Service<CustomAction> {
	
	public void process(CustomAction action) {
	}
}
