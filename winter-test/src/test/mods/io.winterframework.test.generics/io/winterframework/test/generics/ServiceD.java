package io.winterframework.test.generics;

import io.winterframework.core.annotation.Bean;

@Bean
public class ServiceD implements Service<CustomAction> {
	
	public void process(CustomAction action) {
	}
}
