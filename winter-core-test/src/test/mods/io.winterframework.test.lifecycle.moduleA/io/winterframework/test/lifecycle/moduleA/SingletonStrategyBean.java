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
package io.winterframework.test.lifecycle.moduleA;

import io.winterframework.core.annotation.Bean;
import io.winterframework.core.annotation.Bean.Strategy;
import io.winterframework.core.annotation.Destroy;
import io.winterframework.core.annotation.Init;

@Bean(strategy=Strategy.SINGLETON)
public class SingletonStrategyBean {

	public int initCount;
	
	public int destroyCount;
	
	public boolean beanInjected;
	
	public InjectedBean bean;
	
	public SingletonStrategyBean(InjectedBean bean) {
		this.bean = bean;
	}
	
	@Init
	public void init() {
		this.initCount++;
		this.beanInjected = this.bean != null;
	}
	
	@Destroy
	public void destroy() {
		this.destroyCount++;
	}
}
