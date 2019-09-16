package io.winterframework.test.socketbean.moduleA;

import java.util.List;
import java.util.function.Supplier;

import io.winterframework.core.annotation.Bean;
import com.sun.net.httpserver.HttpHandler;

@Bean
public interface ExtHttpHandlerList extends Supplier<List<HttpHandler>>{

}
