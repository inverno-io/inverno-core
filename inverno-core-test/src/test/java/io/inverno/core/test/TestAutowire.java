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
package io.inverno.core.test;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import io.inverno.test.InvernoCompilationException;
import io.inverno.test.InvernoModuleException;
import io.inverno.test.InvernoModuleProxy;

/**
 * 
 * @author <a href="mailto:jeremy.kuhn@inverno.io">Jeremy Kuhn</a>
 *
 */
public class TestAutowire extends AbstractCoreInvernoTest {

	private static final String MODULE = "io.inverno.core.test.autowire";
	
	private InvernoModuleProxy moduleProxy;
	
	@BeforeEach
	public void init() throws IOException, InvernoCompilationException {
		try {
			if(this.moduleProxy == null) {
				this.moduleProxy = this.getInvernoCompiler().compile(MODULE).load(MODULE).build();
			}
		} catch (InvernoModuleException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvernoCompilationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	@Test
	public void testRequiredWire() throws IllegalArgumentException, IllegalAccessException, NoSuchFieldException, SecurityException {
		this.moduleProxy.start();
		
		Object beanA = this.moduleProxy.getBean("beanA");
		Object beanB = this.moduleProxy.getBean("beanB");
		
		Assertions.assertNotNull(beanA);
		Assertions.assertNotNull(beanB);
		
		Object beanA_beanB = beanA.getClass().getField("beanB").get(beanA);
		
		Assertions.assertNotNull(beanA_beanB);
		Assertions.assertEquals(beanB, beanA_beanB);
		
		this.moduleProxy.stop();
	}
	
	@Test
	public void testOptionalWire() throws IllegalArgumentException, IllegalAccessException, NoSuchFieldException, SecurityException {
		this.moduleProxy.start();
		
		Object beanA = this.moduleProxy.getBean("beanA");
		Object beanD = this.moduleProxy.getBean("beanD");
		
		Assertions.assertNotNull(beanA);
		Assertions.assertNotNull(beanD);
		
		Object beanA_beanD = beanA.getClass().getField("beanD").get(beanA);
		Object beanA_optionalRunable = beanA.getClass().getField("optionalRunnable").get(beanA);
		
		Assertions.assertNotNull(beanA_beanD);
		Assertions.assertEquals(beanD, beanA_beanD);
		Assertions.assertNull(beanA_optionalRunable);
		
		this.moduleProxy.stop();
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Test
	public void testMultiWire() throws IllegalArgumentException, IllegalAccessException, NoSuchFieldException, SecurityException {
		this.moduleProxy.start();
		
		Object beanA = this.moduleProxy.getBean("beanA");
		Object serviceA1 = this.moduleProxy.getBean("serviceA1");
		Object serviceA2 = this.moduleProxy.getBean("serviceA2");
		Object serviceB1 = this.moduleProxy.getBean("serviceB1");
		Object serviceB2 = this.moduleProxy.getBean("serviceB2");

		Assertions.assertNotNull(beanA);
		Assertions.assertNotNull(serviceA1);
		Assertions.assertNotNull(serviceA2);
		Assertions.assertNotNull(serviceB1);
		Assertions.assertNotNull(serviceB2);
		
		Object beanA_serviceAList = beanA.getClass().getField("serviceAList").get(beanA);
		Object beanA_serviceAArray = beanA.getClass().getField("serviceAArray").get(beanA);
		Object beanA_serviceASet = beanA.getClass().getField("serviceASet").get(beanA);
		Object beanA_serviceACollection = beanA.getClass().getField("serviceACollection").get(beanA);
		
		Assertions.assertNotNull(beanA_serviceAList);
		Assertions.assertEquals(2, ((List)beanA_serviceAList).size());
		Assertions.assertTrue(((List)beanA_serviceAList).containsAll(Arrays.asList(serviceA1, serviceA2)));
		
		Assertions.assertNotNull(beanA_serviceAArray);
		Assertions.assertEquals(2, ((Object[])beanA_serviceAArray).length);
		Assertions.assertTrue(Arrays.asList(((Object[])beanA_serviceAArray)).containsAll(Arrays.asList(serviceA1, serviceA2)));
		
		Assertions.assertNotNull(beanA_serviceASet);
		Assertions.assertEquals(2, ((Set)beanA_serviceASet).size());
		Assertions.assertTrue(((Set)beanA_serviceASet).containsAll(Arrays.asList(serviceA1, serviceA2)));
		
		Assertions.assertNotNull(beanA_serviceACollection);
		Assertions.assertEquals(2, ((Collection)beanA_serviceACollection).size());
		Assertions.assertTrue(((Collection)beanA_serviceACollection).containsAll(Arrays.asList(serviceA1, serviceA2)));
		
		Object beanA_serviceBList = beanA.getClass().getField("serviceBList").get(beanA);
		Object beanA_serviceBArray = beanA.getClass().getField("serviceBArray").get(beanA);
		Object beanA_serviceBSet = beanA.getClass().getField("serviceBSet").get(beanA);
		Object beanA_serviceBCollection = beanA.getClass().getField("serviceBCollection").get(beanA);
		
		Assertions.assertNotNull(beanA_serviceBList);
		Assertions.assertEquals(2, ((List)beanA_serviceBList).size());
		Assertions.assertTrue(((List)beanA_serviceBList).containsAll(Arrays.asList(serviceB1, serviceB2)));
		
		Assertions.assertNotNull(beanA_serviceBArray);
		Assertions.assertEquals(2, ((Object[])beanA_serviceBArray).length);
		Assertions.assertTrue(Arrays.asList(((Object[])beanA_serviceBArray)).containsAll(Arrays.asList(serviceB1, serviceB2)));
		
		Assertions.assertNotNull(beanA_serviceASet);
		Assertions.assertEquals(2, ((Set)beanA_serviceBSet).size());
		Assertions.assertTrue(((Set)beanA_serviceBSet).containsAll(Arrays.asList(serviceB1, serviceB2)));
		
		Assertions.assertNotNull(beanA_serviceBCollection);
		Assertions.assertEquals(2, ((Collection)beanA_serviceBCollection).size());
		Assertions.assertTrue(((Collection)beanA_serviceBCollection).containsAll(Arrays.asList(serviceB1, serviceB2)));
		
		this.moduleProxy.stop();
	}
	
	@Test
	public void testExplicitSocketWire() throws IllegalArgumentException, IllegalAccessException, NoSuchFieldException, SecurityException {
		this.moduleProxy.start();

		Object beanE = this.moduleProxy.getBean("beanE");
		Object beanA = this.moduleProxy.getBean("beanA");
		Object beanB = this.moduleProxy.getBean("beanB");
		
		Object beanE_beanA = beanE.getClass().getField("beanA").get(beanE);
		Object beanE_beanB = beanE.getClass().getField("beanB").get(beanE);
		Object beanE_beanC = beanE.getClass().getField("beanC").get(beanE);
		Object beanE_beanD = beanE.getClass().getField("beanD").get(beanE);
		
		Assertions.assertNotNull(beanE_beanA);
		Assertions.assertEquals(beanA, beanE_beanA);
		Assertions.assertNotNull(beanE_beanB);
		Assertions.assertEquals(beanB, beanE_beanB);
		
		Assertions.assertNull(beanE_beanC);
		Assertions.assertNull(beanE_beanD);
		
		this.moduleProxy.stop();
	}
	
	@Test
	public void testGenericsWire() throws IllegalArgumentException, IllegalAccessException, NoSuchFieldException, SecurityException {
		this.moduleProxy.start();

		Object beanB = this.moduleProxy.getBean("beanB");
		Object serviceGenString = this.moduleProxy.getBean("serviceGenString");
		Object serviceGenInteger = this.moduleProxy.getBean("serviceGenInteger");
		
		Assertions.assertNotNull(beanB);
		
		Object beanB_serviceGenString = beanB.getClass().getField("serviceGenString").get(beanB);
		Object beanB_serviceGenInteger = beanB.getClass().getField("serviceGenInteger").get(beanB);
		Object beanB_serviceGenDouble = beanB.getClass().getField("serviceGenDouble").get(beanB);
		
		Assertions.assertNotNull(beanB_serviceGenString);
		Assertions.assertEquals(serviceGenString, beanB_serviceGenString);
		Assertions.assertNotNull(beanB_serviceGenInteger);
		Assertions.assertEquals(serviceGenInteger, beanB_serviceGenInteger);
		Assertions.assertNull(beanB_serviceGenDouble);
		
		this.moduleProxy.stop();
	}
	
	@Test
	public void testPrototypeWire() throws IllegalArgumentException, IllegalAccessException, NoSuchFieldException, SecurityException {
		this.moduleProxy.start();

		Object serviceA1 = this.moduleProxy.getBean("serviceA1");
		Object serviceA2 = this.moduleProxy.getBean("serviceA2");
		
		Assertions.assertNotNull(serviceA1);
		Assertions.assertNotNull(serviceA1.getClass().getField("beanC").get(serviceA1));
		Assertions.assertNotNull(serviceA2);
		Assertions.assertNotNull(serviceA2.getClass().getField("beanC").get(serviceA2));
		Assertions.assertNotEquals(serviceA1.getClass().getField("beanC").get(serviceA1), serviceA2.getClass().getField("beanC").get(serviceA2));
		
		this.moduleProxy.stop();
	}
	
	@Test
	public void testSingletonWire() throws IOException, IllegalArgumentException, IllegalAccessException, NoSuchFieldException, SecurityException {
		this.moduleProxy.start();

		Object beanD = this.moduleProxy.getBean("beanD");
		Object serviceB1 = this.moduleProxy.getBean("serviceB1");
		Object serviceB2 = this.moduleProxy.getBean("serviceB2");
		
		Assertions.assertNotNull(beanD);
		Assertions.assertNotNull(serviceB1);
		Assertions.assertNotNull(serviceB1.getClass().getField("beanD").get(serviceB1));
		Assertions.assertNotNull(serviceB2);
		Assertions.assertNotNull(serviceB2.getClass().getField("beanD").get(serviceB2));
		Assertions.assertEquals(beanD, serviceB1.getClass().getField("beanD").get(serviceB1));
		Assertions.assertEquals(beanD, serviceB2.getClass().getField("beanD").get(serviceB2));
		
		this.moduleProxy.stop();
	}
}
