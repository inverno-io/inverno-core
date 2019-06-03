package io.winterframework.test.multicycle.moduleB;

import java.util.concurrent.Callable;

import io.winterframework.core.annotation.Bean;

@Bean
public class BeanA implements Callable<String> {

	public Runnable runnable;
	
	public BeanA(Runnable runnable) {
		this.runnable = runnable;
	}
	
	public String call() {
		
	}
}
