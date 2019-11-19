package io.winterframework.test.explicitwire.moduleF;

import java.util.function.Supplier;

import io.winterframework.core.annotation.Bean;

@Bean
public interface RunnableSocket extends Supplier<Runnable> {

}
