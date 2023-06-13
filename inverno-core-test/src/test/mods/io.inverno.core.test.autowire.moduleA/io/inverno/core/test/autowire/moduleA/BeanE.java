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
import io.inverno.core.annotation.BeanSocket;

@Bean
public class BeanE {

	public BeanA beanA;

	public BeanB beanB;
	
	public BeanC beanC;
	
	public BeanD beanD;
	
	@BeanSocket
	public BeanE(BeanA beanA) {
		this.beanA = beanA;
	}
	
	public BeanE(BeanA beanA, BeanD beanD) {
		this.beanA = beanA;
		this.beanD = beanD;
	}
	
	@BeanSocket
	public void setBeanB(BeanB beanB) {
		this.beanB = beanB;
	}
	
	public void setBeanC(BeanC beanC) {
		this.beanC = beanC;
	}
}
