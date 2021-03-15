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
package io.winterframework.core.test;

import java.io.IOException;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import io.winterframework.test.WinterCompilationException;
import io.winterframework.test.WinterModuleProxy;

/**
 * 
 * @author <a href="mailto:jeremy.kuhn@winterframework.io">Jeremy Kuhn</a>
 *
 */
public class TestBeanStrategy extends AbstractCoreWinterTest {

	private static final String MODULE = "io.winterframework.test.strategy";
	
	private WinterModuleProxy strategyModuleProxy;
	
	@BeforeEach
	public void init() throws IOException, WinterCompilationException {
		if(this.strategyModuleProxy == null) {
			this.strategyModuleProxy = this.getWinterCompiler().compile(MODULE).load(MODULE).build();
		}
	}
	
	@Test
	public void testDefaultStrategy() {
		try {
			this.strategyModuleProxy.start();
			Assertions.assertNotNull(this.strategyModuleProxy.getBean("defaultStrategyBean"));
			Assertions.assertEquals(this.strategyModuleProxy.getBean("defaultStrategyBean"), this.strategyModuleProxy.getBean("defaultStrategyBean"));
		}
		finally {
			this.strategyModuleProxy.stop();
		}
	}
	
	@Test
	public void testSingletonStrategy() {
		try {
			this.strategyModuleProxy.start();
			Assertions.assertNotNull(this.strategyModuleProxy.getBean("singletonStrategyBean"));
			Assertions.assertEquals(this.strategyModuleProxy.getBean("singletonStrategyBean"), this.strategyModuleProxy.getBean("singletonStrategyBean"));
		}
		finally {
			this.strategyModuleProxy.stop();
		}
	}
	
	@Test
	public void testPrototypeStrategy() {
		try {
			this.strategyModuleProxy.start();
			Assertions.assertNotNull(this.strategyModuleProxy.getBean("prototypeStrategyBean"));
			Assertions.assertNotEquals(this.strategyModuleProxy.getBean("prototypeStrategyBean"), this.strategyModuleProxy.getBean("prototypeStrategyBean"));
		}
		finally {
			this.strategyModuleProxy.stop();
		}
	}
}
