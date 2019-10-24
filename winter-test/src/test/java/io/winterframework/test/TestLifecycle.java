package io.winterframework.test;

import java.io.IOException;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import io.winterframework.core.test.AbstractWinterTest;
import io.winterframework.core.test.WinterCompilationException;
import io.winterframework.core.test.WinterModuleProxy;

public class TestLifecycle extends AbstractWinterTest {

	private static final String MODULEA = "io.winterframework.test.lifecycle.moduleA";
	private static final String MODULEB = "io.winterframework.test.lifecycle.moduleB";
	
	@Test
	public void testInitDestroy() throws IllegalArgumentException, IllegalAccessException, NoSuchFieldException, SecurityException, IOException, WinterCompilationException {
		WinterModuleProxy moduleA = this.getWinterCompiler().compile(MODULEA).load(MODULEA).build();
		
		Object singletonBean = null, prototypeBean1 = null, prototypeBean2 = null;
		try {
			moduleA.start();
			
			singletonBean = moduleA.getBean("singletonScopeBean");
			prototypeBean1 = moduleA.getBean("prototypeScopeBean");
			prototypeBean2 = moduleA.getBean("prototypeScopeBean");

			Assertions.assertEquals(1, singletonBean.getClass().getField("initCount").get(singletonBean));
			Assertions.assertEquals(1, prototypeBean1.getClass().getField("initCount").get(prototypeBean1));
			Assertions.assertEquals(1, prototypeBean2.getClass().getField("initCount").get(prototypeBean2));
		} 
		finally {
			moduleA.stop();
			Assertions.assertEquals(1, singletonBean.getClass().getField("destroyCount").get(singletonBean));
			Assertions.assertEquals(1, prototypeBean1.getClass().getField("destroyCount").get(prototypeBean1));
			Assertions.assertEquals(1, prototypeBean2.getClass().getField("destroyCount").get(prototypeBean2));
		}
	}
	
	@Test
	public void testInitAfterDI() throws IllegalArgumentException, IllegalAccessException, NoSuchFieldException, SecurityException, IOException, WinterCompilationException {
		WinterModuleProxy moduleA = this.getWinterCompiler().compile(MODULEA).load(MODULEA).build();
		
		Object singletonBean = null, prototypeBean1 = null, prototypeBean2 = null;
		try {
			moduleA.start();
			
			singletonBean = moduleA.getBean("singletonScopeBean");
			prototypeBean1 = moduleA.getBean("prototypeScopeBean");
			prototypeBean2 = moduleA.getBean("prototypeScopeBean");

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
