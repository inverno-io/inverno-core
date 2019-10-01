package io.winterframework.test.includeexclude.moduleC;

import io.winterframework.core.annotation.Bean;
import java.util.concurrent.Callable;

@Bean
public class BeanC {

	public Runnable runnable;
	
	public Callable<String> callable;
	
	public void setRunnable(Runnable runnable) {
		this.runnable = runnable;
	}
	
	public void setCallable(Callable<String> callable) {
		this.callable = callable;
	}
}
