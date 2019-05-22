package io.winterframework.test.socketbean;

import java.util.Set;
import java.util.concurrent.Callable;
import java.util.function.Supplier;

import io.winterframework.core.annotation.Bean;

@Bean
public interface ExtCallableSet extends Supplier<Set<Callable<String>>> {

}
