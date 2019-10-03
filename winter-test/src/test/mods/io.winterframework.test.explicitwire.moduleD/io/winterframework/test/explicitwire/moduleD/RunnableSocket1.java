package io.winterframework.test.explicitwire.moduleD;

import java.util.function.Supplier;

import io.winterframework.core.annotation.Bean;

@Bean
public interface RunnableSocket1 extends Supplier<Runnable> {

}
