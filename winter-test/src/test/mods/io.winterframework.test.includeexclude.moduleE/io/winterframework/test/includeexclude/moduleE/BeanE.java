package io.winterframework.test.includeexclude.moduleE;

import io.winterframework.core.annotation.Bean;
import java.util.concurrent.Callable;

@Bean
public class BeanE {

	public Runnable runnable;
	
	public Callable<String> callable;
	
	public void setRunnable(Runnable runnable) {
		this.runnable = runnable;
	}
	
	public void setCallable(Callable<String> callable) {
		this.callable = callable;
	}
}
