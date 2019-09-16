package io.winterframework.test.socketbean.moduleA;

import java.util.List;
import java.util.concurrent.Callable;

import javax.sql.DataSource;

import io.winterframework.core.annotation.Bean;

@Bean
public class BeanA {

	public DataSource dataSource;
	
	public List<Callable<String>> callables;
	
	public BeanA(DataSource dataSource, List<Callable<String>> callables) {
		this.dataSource = dataSource;
		this.callables = callables;
	}
}
