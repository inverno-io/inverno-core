package io.winterframework.test.socketbean.moduleA;

import java.util.List;

import com.sun.net.httpserver.HttpHandler;

import io.winterframework.core.annotation.Bean;

@Bean
public class BeanB {

	public Runnable runnable;
	
	public List<HttpHandler> handlers;
	
	public BeanB() {
	}
	
	public void setRunnable(Runnable runnable) {
		this.runnable = runnable;
	}
	
	public void setHttpHandlers(List<HttpHandler> handlers) {
		this.handlers = handlers;
	}
}
