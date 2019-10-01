package io.winterframework.test.includeexclude.moduleB;

import io.winterframework.core.annotation.Bean;
import java.util.concurrent.Callable;

@Bean
public class BeanB implements Callable<String> {
	

	public BeanB() {
	}

	public String call() throws Exception {
		return null;
	}
}
