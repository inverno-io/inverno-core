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
package io.winterframework.test;

import java.io.IOException;
import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import io.winterframework.core.test.AbstractWinterTest;
import io.winterframework.core.test.WinterCompilationException;
import io.winterframework.core.test.WinterModuleProxy;

/**
 * 
 * @author jkuhn
 *
 */
public class TestGenerics extends AbstractWinterTest {
	
	private static final String MODULE = "io.winterframework.test.generics";
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Test
	public void testGenerics() throws IllegalArgumentException, IllegalAccessException, NoSuchFieldException, SecurityException, IOException, WinterCompilationException {
		WinterModuleProxy genericsProxy = this.getWinterCompiler().compile(MODULE).load(MODULE).build();
		
		try {
			genericsProxy.start();
			
			Object beanA = genericsProxy.getBean("beanA");
			
			Assertions.assertNotNull(beanA);
			
			Object serviceA = genericsProxy.getBean("serviceA");
			Object serviceB = genericsProxy.getBean("serviceB");
			Object serviceC = genericsProxy.getBean("serviceC");
			Object serviceD = genericsProxy.getBean("serviceD");
			Object serviceE = genericsProxy.getBean("serviceE");
			Object serviceF = genericsProxy.getBean("serviceF");
			
			Assertions.assertNotNull(serviceA);
			Assertions.assertNotNull(serviceB);
			Assertions.assertNotNull(serviceC);
			Assertions.assertNotNull(serviceD);
			Assertions.assertNotNull(serviceE);
			Assertions.assertNotNull(serviceF);
			
			// ABE
			Object beanA_servicesAction = beanA.getClass().getField("servicesAction").get(beanA);
			// ABCDEF
			Object beanA_servicesExtendAction = beanA.getClass().getField("servicesExtendAction").get(beanA);
			// CDF
			Object beanA_servicesCustomAction = beanA.getClass().getField("servicesCustomAction").get(beanA);
			// CDF
			Object beanA_servicesExtendCustomAction = beanA.getClass().getField("servicesExtendCustomAction").get(beanA);
			// E
			Object beanA_customServicesAction = beanA.getClass().getField("customServicesAction").get(beanA);
			// EF
			Object beanA_customServicesExtendsAction = beanA.getClass().getField("customServicesExtendsAction").get(beanA);
			// F
			Object beanA_customServicesCustomAction = beanA.getClass().getField("customServicesCustomAction").get(beanA);
			// F
			Object beanA_customServicesExtendsCustomAction = beanA.getClass().getField("customServicesExtendsCustomAction").get(beanA);
			
			Assertions.assertNotNull(beanA_servicesAction);
			Assertions.assertNotNull(beanA_servicesExtendAction);
			Assertions.assertNotNull(beanA_servicesCustomAction);
			Assertions.assertNotNull(beanA_servicesExtendCustomAction);
			Assertions.assertNotNull(beanA_customServicesAction);
			Assertions.assertNotNull(beanA_customServicesExtendsAction);
			Assertions.assertNotNull(beanA_customServicesCustomAction);
			Assertions.assertNotNull(beanA_customServicesExtendsCustomAction);
			
			Assertions.assertTrue(((List)beanA_servicesAction).containsAll(List.of(serviceA, serviceB, serviceE)));
			Assertions.assertTrue(((List)beanA_servicesExtendAction).containsAll(List.of(serviceA, serviceB, serviceC, serviceD, serviceE, serviceF)));
			Assertions.assertTrue(((List)beanA_servicesCustomAction).containsAll(List.of(serviceC, serviceD, serviceF)));
			Assertions.assertTrue(((List)beanA_servicesExtendCustomAction).containsAll(List.of(serviceC, serviceD, serviceF)));
			Assertions.assertTrue(((List)beanA_customServicesAction).containsAll(List.of(serviceE)));
			Assertions.assertTrue(((List)beanA_customServicesExtendsAction).containsAll(List.of(serviceE, serviceF)));
			Assertions.assertTrue(((List)beanA_customServicesCustomAction).containsAll(List.of(serviceF)));
			Assertions.assertTrue(((List)beanA_customServicesExtendsCustomAction).containsAll(List.of(serviceF)));
		}
		finally {
			genericsProxy.stop();
		}
	}
}
