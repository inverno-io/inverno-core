package io.winterframework.test.error.moduleG;

import io.winterframework.core.annotation.Bean;
import java.util.function.Supplier;

@Bean(name="beanD")
public class RunnableSocket implements Supplier<Runnable> {
	
}
