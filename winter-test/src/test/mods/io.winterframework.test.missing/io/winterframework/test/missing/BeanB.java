package io.winterframework.test.missing;

import java.util.List;

import javax.sql.DataSource;

import io.winterframework.core.annotation.Bean;

@Bean
public class BeanB {

	public List<DataSource> dataSources;
	
	public BeanB(List<DataSource> dataSources) {
		this.dataSources = dataSources;
	}
}
