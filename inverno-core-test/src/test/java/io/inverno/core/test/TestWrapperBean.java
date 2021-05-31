/*
 * Copyright 2020 Jeremy KUHN
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
package io.inverno.core.test;

import java.io.IOException;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import io.inverno.test.InvernoCompilationException;
import io.inverno.test.InvernoModuleProxy;

/**
 * 
 * @author <a href="mailto:jeremy.kuhn@inverno.io">Jeremy Kuhn</a>
 *
 */
public class TestWrapperBean extends AbstractCoreInvernoTest {

	private static final String MODULE = "io.inverno.core.test.wrapperbean";
	
	@Test
	public void testBeanCreation() throws IOException, InvernoCompilationException {
		InvernoModuleProxy simpleProxy = this.getInvernoCompiler().compile(MODULE).load(MODULE).build();
		
		simpleProxy.start();
		Assertions.assertNotNull(simpleProxy.getBean("beanA"));
		Assertions.assertEquals(simpleProxy.getBean("beanA"), simpleProxy.getBean("beanA"));
		Assertions.assertTrue(simpleProxy.getBean("beanA") instanceof Runnable);
		simpleProxy.stop();
	}
}
