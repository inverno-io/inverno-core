package io.winterframework.test.explicitwire.moduleD;

import io.winterframework.core.annotation.Bean;
import java.util.concurrent.Callable;

@Bean
public class CallableC implements Callable<String> {

	@Override
	public String call() {
		return null;
	}
}