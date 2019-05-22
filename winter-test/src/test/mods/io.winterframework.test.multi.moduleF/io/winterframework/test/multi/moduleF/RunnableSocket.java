package io.winterframework.test.multi.moduleF;

import io.winterframework.core.annotation.Bean;
import java.util.function.Supplier;

@Bean
public interface RunnableSocket extends Supplier<Runnable> {
	
}
