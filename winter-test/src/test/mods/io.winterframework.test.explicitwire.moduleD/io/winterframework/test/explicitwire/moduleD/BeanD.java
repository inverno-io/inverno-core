package io.winterframework.test.explicitwire.moduleD;

import java.util.List;

import java.util.concurrent.Callable;

import io.winterframework.core.annotation.Bean;

@Bean
public class BeanD {

	public Runnable runnable1;
	
	public Runnable runnable2;
	
	public List<Runnable> runnables;
	
	public Callable<String> callableA;
	
	public Callable<String> callableB;
	
	public Callable<String> callableC;
	
	public BeanD(Runnable runnable1, Runnable runnable2, List<Runnable> runnables, Callable<String> callableA, Callable<String> callableB, Callable<String> callableC) {
		this.runnable1 = runnable1;
		this.runnable2 = runnable2;
		this.runnables = runnables;
		
		this.callableA = callableA;
		this.callableB = callableB;
		this.callableC = callableC;
	}
}
