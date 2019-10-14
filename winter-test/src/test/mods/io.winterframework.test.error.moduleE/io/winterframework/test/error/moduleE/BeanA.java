package io.winterframework.test.error.moduleE;

import io.winterframework.core.annotation.Bean;
import io.winterframework.core.annotation.BeanSocket;

@Bean
public class BeanA {
	
	@BeanSocket
	public BeanA() {
	}
	
	@BeanSocket
	public BeanA(Runnable r) {
	}
}
