package io.winterframework.test.socketbean.moduleA;

import java.util.List;
import java.util.concurrent.Callable;
import java.util.function.Supplier;

import io.winterframework.core.annotation.Bean;

@Bean
public interface ExtCallableList extends Supplier<List<Callable<String>>> {

}
