package io.winterframework.test.autowire;

import io.winterframework.core.annotation.Bean;

import java.util.Collection;
import java.util.List;
import java.util.Set;

@Bean
public class BeanA {

	public Runnable optionalRunnable;
	
	public BeanB beanB;
	
	public BeanD beanD;
	
	public List<ServiceA> serviceAList;
	public ServiceA[] serviceAArray;
	public Set<ServiceA> serviceASet;
	public Collection<ServiceA> serviceACollection;
	
	public List<ServiceB> serviceBList;
	public ServiceB[] serviceBArray;
	public Set<ServiceB> serviceBSet;
	public Collection<ServiceB> serviceBCollection;
	
	public BeanA(BeanB beanB, List<ServiceA> serviceAList, ServiceA[] serviceAArray, Set<ServiceA> serviceASet, Collection<ServiceA> serviceACollection) {
		this.beanB = beanB;
		
		this.serviceAList = serviceAList;
		this.serviceAArray = serviceAArray;
		this.serviceASet = serviceASet;
		this.serviceACollection = serviceACollection;
	}
	
	public void setOptionalBeanD(BeanD beanD) {
		this.beanD = beanD;
	}
	
	public void setOptionalRunnable(Runnable runnable) {
		this.optionalRunnable = runnable;
	}
	
	public void setServiceBList(List<ServiceB> serviceBList) {
		this.serviceBList = serviceBList;
	}
	
	public void setServiceBArray(ServiceB[] serviceBArray) {
		this.serviceBArray= serviceBArray; 
	}
	
	public void setServiceBSet(Set<ServiceB> serviceBSet) {
		this.serviceBSet = serviceBSet;
	}

	public void setServiceBCollection(Collection<ServiceB> serviceBCollection) {
		this.serviceBCollection = serviceBCollection;
	}
}
