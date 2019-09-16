package io.winterframework.test.socketbean.moduleA;

import java.util.Collection;
import java.util.concurrent.Callable;
import java.util.function.Supplier;

import io.winterframework.core.annotation.Bean;

@Bean
public interface ExtCallableCollection extends Supplier<Collection<Callable<String>>> {

}
