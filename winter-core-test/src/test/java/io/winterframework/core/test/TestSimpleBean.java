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
import org.junit.jupiter.api.Test;

import io.winterframework.test.WinterCompilationException;
import io.winterframework.test.WinterModuleProxy;

/**
 * 
 * @author <a href="mailto:jeremy.kuhn@winterframework.io">Jeremy Kuhn</a>
 *
 */
public class TestSimpleBean extends AbstractCoreWinterTest {

	private static final String MODULE = "io.winterframework.test.simplebean";
	
	@Test
	public void testBeanCreation() throws IOException, WinterCompilationException, IllegalArgumentException, IllegalAccessException, NoSuchFieldException, SecurityException {
		Runnable someRunnable = () -> {};
		
		WinterModuleProxy simpleProxy = this.getWinterCompiler().compile(MODULE).load(MODULE).dependencies(someRunnable).build();
		
		simpleProxy.start();
		Assertions.assertNotNull(simpleProxy.getBean("beanA"));
		Assertions.assertEquals(simpleProxy.getBean("beanA"), simpleProxy.getBean("beanA"));
		Assertions.assertNotNull(simpleProxy.getBean("beanB"));
		
		Object beanB = simpleProxy.getBean("beanB");
		Object beanB_someRunnable = beanB.getClass().getField("someRunnable").get(beanB);
		Assertions.assertEquals(someRunnable, beanB_someRunnable);
		
		Assertions.assertNotNull(simpleProxy.getBean("beanA"));
		simpleProxy.stop();
	}
}
