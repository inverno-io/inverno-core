package io.winterframework.test.socketbean.moduleF;

import io.winterframework.core.annotation.Bean;

import java.util.function.Supplier;

@Bean(name="# invalid 123")
public interface RunnableSocket extends Supplier<Runnable> {

}
