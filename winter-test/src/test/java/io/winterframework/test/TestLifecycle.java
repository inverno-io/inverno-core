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

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import io.winterframework.core.test.WinterCompilationException;
import io.winterframework.core.test.WinterModuleProxy;

/**
 * 
 * @author jkuhn
 *
 */
public class TestLifecycle extends AbstractCoreWinterTest {

	private static final String MODULEA = "io.winterframework.test.lifecycle.moduleA";
	private static final String MODULEB = "io.winterframework.test.lifecycle.moduleB";
	private static final String MODULEC = "io.winterframework.test.lifecycle.moduleC";
	
	@Test
	public void testInitDestroy() throws IllegalArgumentException, IllegalAccessException, NoSuchFieldException, SecurityException, IOException, WinterCompilationException, InterruptedException {
		WinterModuleProxy moduleA = this.getWinterCompiler().compile(MODULEA).load(MODULEA).build();
		
		@SuppressWarnings("unused")
		Object singletonBean = null, prototypeBean1 = null, prototypeBean2 = null, prototypeBean3 = null;
		try {
			moduleA.start();
			
			singletonBean = moduleA.getBean("singletonStrategyBean");
			prototypeBean1 = moduleA.getBean("prototypeStrategyBean");
			prototypeBean2 = moduleA.getBean("prototypeStrategyBean");
			prototypeBean3 = moduleA.getBean("prototypeStrategyBean");
			prototypeBean3 = null;
			
			System.gc();
			
			Assertions.assertEquals(1, singletonBean.getClass().getField("initCount").get(singletonBean));
			Assertions.assertEquals(1, prototypeBean1.getClass().getField("initCount").get(prototypeBean1));
			Assertions.assertEquals(1, prototypeBean2.getClass().getField("initCount").get(prototypeBean2));
			Assertions.assertEquals(3, prototypeBean2.getClass().getField("globalInitCount").get(prototypeBean2));
		} 
		finally {
			moduleA.stop();
			Assertions.assertEquals(1, singletonBean.getClass().getField("destroyCount").get(singletonBean));
			Assertions.assertEquals(1, prototypeBean1.getClass().getField("destroyCount").get(prototypeBean1));
			Assertions.assertEquals(1, prototypeBean2.getClass().getField("destroyCount").get(prototypeBean2));
			// Only two are destroyed since the third one is no longer referenced and GC has been invoked
			Assertions.assertEquals(2, prototypeBean2.getClass().getField("globalDestroyCount").get(null));
		}
	}
	
	@Test
	public void testInitDestroyWrapper() throws IllegalArgumentException, IllegalAccessException, NoSuchFieldException, SecurityException, IOException, WinterCompilationException, InterruptedException {
		WinterModuleProxy moduleA = this.getWinterCompiler().compile(MODULEC).load(MODULEC).build();
		
		@SuppressWarnings("unused")
		Object singletonBean = null, prototypeBean1 = null, prototypeBean2 = null, prototypeBean3 = null;
		try {
			moduleA.start();
			
			singletonBean = moduleA.getBean("singletonStrategyWrapperBean");
			prototypeBean1 = moduleA.getBean("prototypeStrategyWrapperBean");
			prototypeBean2 = moduleA.getBean("prototypeStrategyWrapperBean");
			prototypeBean3 = moduleA.getBean("prototypeStrategyWrapperBean");
			prototypeBean3 = null;
			
			System.gc();
			
			Assertions.assertEquals(1, singletonBean.getClass().getField("initCount").get(singletonBean));
			Assertions.assertEquals(1, prototypeBean1.getClass().getField("initCount").get(prototypeBean1));
			Assertions.assertEquals(1, prototypeBean2.getClass().getField("initCount").get(prototypeBean2));
			Assertions.assertEquals(3, prototypeBean2.getClass().getField("globalInitCount").get(prototypeBean2));
		}
		catch(Exception e) {
			e.printStackTrace();
		}
		finally {
			moduleA.stop();
			Assertions.assertEquals(1, singletonBean.getClass().getField("destroyCount").get(singletonBean));
			Assertions.assertEquals(1, prototypeBean1.getClass().getField("destroyCount").get(prototypeBean1));
			Assertions.assertEquals(1, prototypeBean2.getClass().getField("destroyCount").get(prototypeBean2));
			// Only three are destroyed since the third one is no longer referenced and GC has been invoked
			Assertions.assertEquals(2, prototypeBean2.getClass().getField("globalDestroyCount").get(null));
		}
	}
	
	@Test
	public void testInitAfterDI() throws IllegalArgumentException, IllegalAccessException, NoSuchFieldException, SecurityException, IOException, WinterCompilationException {
		WinterModuleProxy moduleA = this.getWinterCompiler().compile(MODULEA).load(MODULEA).build();
		
		Object singletonBean = null, prototypeBean1 = null, prototypeBean2 = null;
		try {
			moduleA.start();
			
			singletonBean = moduleA.getBean("singletonStrategyBean");
			prototypeBean1 = moduleA.getBean("prototypeStrategyBean");
			prototypeBean2 = moduleA.getBean("prototypeStrategyBean");

			Assertions.assertTrue((boolean)singletonBean.getClass().getField("beanInjected").get(singletonBean));
			Assertions.assertTrue((boolean)prototypeBean1.getClass().getField("beanInjected").get(prototypeBean1));
			Assertions.assertTrue((boolean)prototypeBean2.getClass().getField("beanInjected").get(prototypeBean2));
		} 
		finally {
			moduleA.stop();
		}
	}
	
	@Test
	public void testDestroyWithDI() throws IllegalArgumentException, IllegalAccessException, NoSuchFieldException, SecurityException, IOException, WinterCompilationException {
		WinterModuleProxy moduleB = this.getWinterCompiler().compile(MODULEB).load(MODULEB).build();
		
		Object beanA = null, beanB = null, beanC = null, beanD = null, beanE = null;
		try {
			moduleB.start();
			
			beanA = moduleB.getBean("beanA");
			beanB = moduleB.getBean("beanB");
			beanC = moduleB.getBean("beanC");
			beanD = moduleB.getBean("beanD");
			beanE = moduleB.getBean("beanE");

			Assertions.assertNotNull(beanA);
			Assertions.assertNotNull(beanB);
			Assertions.assertNotNull(beanC);
			Assertions.assertNotNull(beanD);
			Assertions.assertNotNull(beanE);
		} 
		finally {
			moduleB.stop();
			Assertions.assertFalse(beanA.getClass().getField("destroyFailed").getBoolean(beanA));
			Assertions.assertTrue(beanA.getClass().getField("destroyed").getBoolean(beanA));
			Assertions.assertFalse(beanB.getClass().getField("destroyFailed").getBoolean(beanB));
			Assertions.assertTrue(beanB.getClass().getField("destroyed").getBoolean(beanB));
			Assertions.assertFalse(beanC.getClass().getField("destroyFailed").getBoolean(beanC));
			Assertions.assertTrue(beanC.getClass().getField("destroyed").getBoolean(beanC));
			Assertions.assertFalse(beanD.getClass().getField("destroyFailed").getBoolean(beanD));
			Assertions.assertTrue(beanD.getClass().getField("destroyed").getBoolean(beanD));
			Assertions.assertFalse(beanE.getClass().getField("destroyFailed").getBoolean(beanE));
			Assertions.assertTrue(beanE.getClass().getField("destroyed").getBoolean(beanE));
		}
	}
}
