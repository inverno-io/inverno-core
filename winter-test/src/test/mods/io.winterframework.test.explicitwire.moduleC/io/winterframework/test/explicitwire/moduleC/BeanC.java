package io.winterframework.test.explicitwire.moduleC;

import java.util.List;

import io.winterframework.core.annotation.Bean;

import java.util.concurrent.Callable;

import io.winterframework.test.explicitwire.moduleD.BeanD;

@Bean
public class BeanC {

	public BeanD beanD;
	
	public Callable<String> callable1;
	
	public Callable<String> callable2;
	
	public List<Callable<String>> callables;
	
	public BeanC(BeanD beanD, Callable<String> callable1, Callable<String> callable2, List<Callable<String>> callables) {
		this.beanD = beanD;
		this.callable1 = callable1;
		this.callable2 = callable2;
		this.callables = callables;
	}
}
