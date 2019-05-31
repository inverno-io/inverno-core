package io.winterframework.test.generics;

import io.winterframework.core.annotation.Bean;

@Bean
public class ServiceF implements CustomService<CustomAction> {
	
	public void process(CustomAction action) {
	}
	
	public void process(CustomAction action, String argument) {
	}
}
