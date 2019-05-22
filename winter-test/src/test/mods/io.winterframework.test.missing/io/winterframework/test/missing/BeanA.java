package io.winterframework.test.missing;

import javax.sql.DataSource;

import io.winterframework.core.annotation.Bean;

@Bean
public class BeanA {

	public DataSource dataSource;
	
	public BeanA(DataSource dataSource) {
		this.dataSource = dataSource;
	}
}
