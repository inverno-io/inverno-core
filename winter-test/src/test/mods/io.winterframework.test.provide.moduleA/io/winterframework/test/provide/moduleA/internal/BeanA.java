package io.winterframework.test.provide.moduleA.internal;

import java.util.concurrent.Callable;

import io.winterframework.core.annotation.Bean;
import io.winterframework.core.annotation.Provide;

@Bean
public class BeanA implements Runnable, @Provide Callable<String> {

	@Override
	public String call() throws Exception {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public void run() {
		// TODO Auto-generated method stub
	}
}
