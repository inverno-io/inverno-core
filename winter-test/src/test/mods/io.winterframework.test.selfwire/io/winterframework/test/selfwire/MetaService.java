package io.winterframework.test.selfwire;

import java.util.Set;
import java.util.stream.Collectors;

import io.winterframework.core.annotation.Bean;

@Bean
public class MetaService implements Service {

	public Set<Service> services;
	
	public MetaService(Set<Service> services) {
		this.services = services;
	}
	
	public String execute() {
		return this.services.stream().map(Service::execute).collect(Collectors.joining(","));
	}
}
