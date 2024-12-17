package io.inverno.core.test.socketbean.moduleI;

import io.inverno.core.annotation.Bean;

@Bean
public class BeanA {

	public final Runnable runnable;

	public BeanA(Runnable runnable) {
		this.runnable = runnable;
	}
}
