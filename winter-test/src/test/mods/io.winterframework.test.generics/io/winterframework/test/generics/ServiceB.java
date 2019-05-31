package io.winterframework.test.generics;

import io.winterframework.core.annotation.Bean;

@Bean
public class ServiceB implements Service<Action> {
	
	public void process(Action action) {
	}
}
