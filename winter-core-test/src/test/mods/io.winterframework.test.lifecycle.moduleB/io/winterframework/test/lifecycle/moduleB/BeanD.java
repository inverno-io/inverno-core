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
package io.winterframework.test.lifecycle.moduleB;

import io.winterframework.core.annotation.Bean;
import io.winterframework.core.annotation.Destroy;
import io.winterframework.core.annotation.Init;

@Bean
public class BeanD {

	public BeanC beanC;
	public BeanA beanA;
	
	public boolean destroyed;
	
	public boolean destroyFailed;
	
	public BeanD(BeanC beanC, BeanA beanA) {
		this.beanC = beanC;
		this.beanA = beanA;
	}
	
	@Destroy
	public void destroy() {
		this.destroyFailed = this.beanC.destroyed || this.beanA.destroyed;
		this.destroyed = true;
	}
}