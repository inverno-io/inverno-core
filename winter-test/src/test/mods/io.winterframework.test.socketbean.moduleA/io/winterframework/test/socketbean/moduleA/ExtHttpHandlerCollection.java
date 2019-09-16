package io.winterframework.test.socketbean.moduleA;

import java.util.Collection;
import java.util.function.Supplier;

import com.sun.net.httpserver.HttpHandler;

import io.winterframework.core.annotation.Bean;

@Bean
public interface ExtHttpHandlerCollection extends Supplier<Collection<HttpHandler>>{

}
