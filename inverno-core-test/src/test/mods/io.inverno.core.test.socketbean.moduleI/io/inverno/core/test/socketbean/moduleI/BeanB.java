package io.inverno.core.test.socketbean.moduleI;

import io.inverno.core.annotation.Bean;

@Bean
public class BeanB {

	public Runnable runnable;

	public BeanB() {
	}

	public void setRunnable(Runnable runnable) {
		this.runnable = runnable;
	}
}
