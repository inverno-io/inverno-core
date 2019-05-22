package io.winterframework.test.socketbean;

import java.util.Set;
import java.util.function.Supplier;

import com.sun.net.httpserver.HttpHandler;

import io.winterframework.core.annotation.Bean;

@Bean
public interface ExtHttpHandlerSet extends Supplier<Set<HttpHandler>>{

}
