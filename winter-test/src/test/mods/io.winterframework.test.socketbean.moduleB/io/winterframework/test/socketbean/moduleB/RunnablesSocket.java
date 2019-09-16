package io.winterframework.test.socketbean.moduleB;

import java.util.List;
import java.util.function.Supplier;

import io.winterframework.core.annotation.Bean;

@Bean
public interface RunnablesSocket extends Supplier<List<Runnable>> {

}
