package io.winterframework.test.socketbean.moduleE;

import io.winterframework.core.annotation.Bean;

import java.util.function.Supplier;

@Bean(visibility = Bean.Visibility.PRIVATE)
public interface RunnableSocket extends Supplier<Runnable> {

}
