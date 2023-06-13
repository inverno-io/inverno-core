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
package io.inverno.core.test.autowire.moduleA;

import io.inverno.core.annotation.Bean;

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
