package io.winterframework.test.provide.moduleB;

import java.util.concurrent.Callable;

import io.winterframework.core.annotation.Bean;

@Bean
public class BeanC {

	public Callable<String> callable;
	
	public Runnable runnable;
	
	public BeanC(Callable<String> callable) {
		this.callable = callable;
	}
	
	public void setRunnable(Runnable runnable) {
		this.runnable = runnable;
	}
}
