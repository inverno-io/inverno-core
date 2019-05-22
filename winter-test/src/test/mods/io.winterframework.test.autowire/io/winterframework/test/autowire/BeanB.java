package io.winterframework.test.autowire;

import io.winterframework.core.annotation.Bean;

@Bean
public class BeanB {

	public ServiceGen<String> serviceGenString;
	
	public ServiceGen<Integer> serviceGenInteger;
	
	public ServiceGen<Double> serviceGenDouble;
	
	public BeanB(ServiceGen<String> serviceGenString) {
		this.serviceGenString = serviceGenString;
	}
	
	public void setServiceGenInteger(ServiceGen<Integer> serviceGenInteger) {
		this.serviceGenInteger = serviceGenInteger;
	}
	
	public void setServiceGenDouble(ServiceGen<Double> serviceGenDouble) {
		this.serviceGenDouble = serviceGenDouble;
	}
}
