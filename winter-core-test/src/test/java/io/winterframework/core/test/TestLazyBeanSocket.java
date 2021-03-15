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
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import io.winterframework.test.WinterCompilationException;
import io.winterframework.test.WinterModuleProxy;

/**
 * 
 * @author <a href="mailto:jeremy.kuhn@winterframework.io">Jeremy Kuhn</a>
 *
 */
public class TestLazyBeanSocket extends AbstractCoreWinterTest {

	private static final String MODULEA = "io.winterframework.test.lazy.moduleA";
	private static final String MODULEB = "io.winterframework.test.lazy.moduleB";
	private static final String MODULEC = "io.winterframework.test.lazy.moduleC";
	private static final String MODULED = "io.winterframework.test.lazy.moduleD";
	
	@Test
	public void testSingleBeanInModule() throws IOException, WinterCompilationException, IllegalArgumentException, IllegalAccessException, NoSuchFieldException, SecurityException {
		WinterModuleProxy moduleProxy = this.getWinterCompiler().compile(MODULEA).load(MODULEA).build();
		
		moduleProxy.start();
		
		Object beanA = moduleProxy.getBean("beanA");
		Object beanB = moduleProxy.getBean("beanB");
		Object beanC = moduleProxy.getBean("beanC");
		
		Assertions.assertNotNull(beanA);
		Assertions.assertNotNull(beanB);
		Assertions.assertNotNull(beanC);
		
		Object beanA_beanB1 = beanA.getClass().getField("beanB1").get(beanA);
		Object beanA_beanB2 = beanA.getClass().getField("beanB2").get(beanA);
		Object beanA_beanC1 = beanA.getClass().getField("beanC1").get(beanA);
		Object beanA_beanC2 = beanA.getClass().getField("beanC2").get(beanA);

		Assertions.assertNotNull(beanA_beanB1);
		Assertions.assertNotNull(beanA_beanB2);
		Assertions.assertNotNull(beanA_beanC1);
		Assertions.assertNotNull(beanA_beanC2);
		
		Assertions.assertEquals(beanB, beanA_beanB1);
		Assertions.assertEquals(beanB, beanA_beanB2);
		
		Assertions.assertNotEquals(beanC, beanA_beanC1);
		Assertions.assertNotEquals(beanC, beanA_beanC2);
		Assertions.assertNotEquals(beanA_beanC1, beanA_beanC2);
		
		moduleProxy.stop();
	}
	
	@Test
	public void testSingleBeanInComponentModule() throws IOException, WinterCompilationException, IllegalArgumentException, IllegalAccessException, NoSuchFieldException, SecurityException {
		WinterModuleProxy moduleProxy = this.getWinterCompiler().compile(MODULEA, MODULEB).load(MODULEB).build();
		
		moduleProxy.start();
		
		Object beanD = moduleProxy.getBean("beanD");
		
		Assertions.assertNotNull(beanD);
		
		Object beanD_beanB1 = beanD.getClass().getField("beanB1").get(beanD);
		Object beanD_beanB2 = beanD.getClass().getField("beanB2").get(beanD);
		Object beanD_beanC1 = beanD.getClass().getField("beanC1").get(beanD);
		Object beanD_beanC2 = beanD.getClass().getField("beanC2").get(beanD);

		Assertions.assertNotNull(beanD_beanB1);
		Assertions.assertNotNull(beanD_beanB2);
		Assertions.assertNotNull(beanD_beanC1);
		Assertions.assertNotNull(beanD_beanC2);
		
		Assertions.assertEquals(beanD_beanB1, beanD_beanB2);
		Assertions.assertNotEquals(beanD_beanC1, beanD_beanC2);
		
		moduleProxy.stop();
	}
	
	@Test
	public void testSingleBeanInModuleSocket() throws IOException, WinterCompilationException, IllegalArgumentException, IllegalAccessException, NoSuchFieldException, SecurityException {
		Runnable r = () -> System.out.println("test");
		WinterModuleProxy moduleProxy = this.getWinterCompiler().compile(MODULEC).load(MODULEC).dependencies(r).build();
		
		moduleProxy.start();
		
		Object beanA = moduleProxy.getBean("beanA");
		
		Object beanA_runnable1 = beanA.getClass().getField("runnable1").get(beanA);
		Object beanA_runnable2 = beanA.getClass().getField("runnable2").get(beanA);

		Assertions.assertNotNull(beanA_runnable1);
		Assertions.assertNotNull(beanA_runnable2);
		
		Assertions.assertEquals(r, beanA_runnable1);
		Assertions.assertEquals(r, beanA_runnable2);
		
		moduleProxy.stop();
	}
	
	@SuppressWarnings("unchecked")
	@Test
	public void testMultiBeanInModuleSocket() throws IOException, WinterCompilationException, IllegalArgumentException, IllegalAccessException, NoSuchFieldException, SecurityException {
		Runnable r = () -> System.out.println("test");
		WinterModuleProxy moduleProxy = this.getWinterCompiler().compile(MODULEA, MODULED).load(MODULED).dependencies(r).build();
		
		moduleProxy.start();
		
		Object beanA = moduleProxy.getBean("beanA");
		
		Object beanA_runnables1 = beanA.getClass().getField("runnables1").get(beanA);
		Object beanA_runnables2 = beanA.getClass().getField("runnables2").get(beanA);

		Assertions.assertNotNull(beanA_runnables1);
		Assertions.assertNotNull(beanA_runnables2);

		List<Runnable> beanA_runnables1_list = new ArrayList<>((List<Runnable>)beanA_runnables1);
		List<Runnable> beanA_runnables2_list = new ArrayList<>((List<Runnable>)beanA_runnables2);
		
		Assertions.assertEquals(3, beanA_runnables1_list.size());
		Assertions.assertEquals(3, beanA_runnables2_list.size());
		
		Assertions.assertTrue(beanA_runnables1_list.remove(r));
		Assertions.assertTrue(beanA_runnables2_list.remove(r));
		
		Runnable beanA_someRunnable1 = null, beanA_myRunnable1 = null;
		for(Runnable runnable : beanA_runnables1_list) {
			if(runnable.getClass().getCanonicalName().equals("io.winterframework.test.lazy.moduleA.SomeRunnable")) {
				beanA_someRunnable1 = runnable;
			}
			else if(runnable.getClass().getCanonicalName().equals("io.winterframework.test.lazy.moduleD.MyRunnable")) {
				beanA_myRunnable1 = runnable;
			}
		}
		
		Runnable beanA_someRunnable2 = null, beanA_myRunnable2 = null;
		for(Runnable runnable : beanA_runnables2_list) {
			if(runnable.getClass().getCanonicalName().equals("io.winterframework.test.lazy.moduleA.SomeRunnable")) {
				beanA_someRunnable2 = runnable;
			}
			else if(runnable.getClass().getCanonicalName().equals("io.winterframework.test.lazy.moduleD.MyRunnable")) {
				beanA_myRunnable2 = runnable;
			}
		}
		
		Assertions.assertNotNull(beanA_someRunnable1);
		Assertions.assertNotNull(beanA_someRunnable2);
		Assertions.assertNotNull(beanA_myRunnable1);
		Assertions.assertNotNull(beanA_myRunnable2);
		
		Assertions.assertEquals(beanA_someRunnable1, beanA_someRunnable2);
		Assertions.assertNotEquals(beanA_myRunnable1, beanA_myRunnable2);
		
		moduleProxy.stop();
	}
}
