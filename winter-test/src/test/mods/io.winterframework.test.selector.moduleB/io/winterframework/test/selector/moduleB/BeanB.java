package io.winterframework.test.selector.moduleB;

import io.winterframework.core.annotation.Bean;

import java.util.List;

@Bean
public class BeanB {

	public Runnable runnable;
	
	public BeanB(Runnable runnable) {
		this.runnable = runnable;
	}
}
