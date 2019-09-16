package io.winterframework.test.socketbean.moduleB;

import java.util.List;

import io.winterframework.core.annotation.Bean;

@Bean
public class BeanB {

	public List<Runnable> runnables;
	
	public BeanB(List<Runnable> runnables) {
		this.runnables = runnables;
	}
}
