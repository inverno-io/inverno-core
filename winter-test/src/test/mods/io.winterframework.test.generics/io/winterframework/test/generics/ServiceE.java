package io.winterframework.test.generics;

import io.winterframework.core.annotation.Bean;

@Bean
public class ServiceE implements CustomService<Action> {
	
	public void process(Action action) {
	}
	
	public void process(Action action, String argument) {
	}
}
