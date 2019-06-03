package io.winterframework.test.multicycle.moduleA;

import java.util.concurrent.Callable;

import io.winterframework.core.annotation.Bean;

@Bean
public class BeanA implements Runnable {

	public Callable<String> callable;
	
	public BeanA(Callable<String> callable) {
		this.callable = callable;
	}
	
	public void run() {
		
	}
}
