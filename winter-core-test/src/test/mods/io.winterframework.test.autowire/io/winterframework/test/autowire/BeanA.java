/*
 * Copyright 2019 Jeremy KUHN
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
