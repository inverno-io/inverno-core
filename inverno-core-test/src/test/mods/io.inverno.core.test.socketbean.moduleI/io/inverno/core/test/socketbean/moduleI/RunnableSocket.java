package io.inverno.core.test.socketbean.moduleI;

import io.inverno.core.annotation.Bean;
import java.util.function.Supplier;

@Bean
public interface RunnableSocket extends Supplier<Runnable> {}
