package io.winterframework.test.simplebean;

import io.winterframework.core.annotation.Bean;

@Bean
public class BeanA {
	
	public BeanA() {
		
	}
	
	public void foo() {
		System.out.println("foo");
	}
}
