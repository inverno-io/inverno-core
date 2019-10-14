package io.winterframework.test.provide.moduleC;

import java.util.concurrent.Callable;

import io.winterframework.core.annotation.Bean;
import io.winterframework.core.annotation.Provide;
import io.winterframework.core.annotation.Wrapper;

import java.util.function.Supplier;

@Bean
@Wrapper
public class BeanB implements Supplier<Callable<String>>, @Provide Runnable {

	public Callable<String> get() {
		return null;
	}
	
	@Override
	public void run() {
		
	}
}
