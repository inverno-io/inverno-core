package io.winterframework.test;

import java.io.IOException;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import io.winterframework.core.test.AbstractWinterTest;
import io.winterframework.core.test.WinterCompilationException;
import io.winterframework.core.test.WinterModuleProxy;

public class TestLifecycle extends AbstractWinterTest {

	private static final String MODULEA = "io.winterframework.test.lifecycle.moduleA";
	
	private WinterModuleProxy lifecycleModuleProxy;
	
	@Test
	public void testInitDestroy() throws IllegalArgumentException, IllegalAccessException, NoSuchFieldException, SecurityException, IOException, WinterCompilationException {
		WinterModuleProxy moduleA = this.getWinterCompiler().compile(MODULEA).load(MODULEA).build();
		
		Object singletonBean = null, prototypeBean1 = null, prototypeBean2 = null;
		try {
			moduleA.start();
			
			singletonBean = this.lifecycleModuleProxy.getBean("singletonScopeBean");
			prototypeBean1 = this.lifecycleModuleProxy.getBean("prototypeScopeBean");
			prototypeBean2 = this.lifecycleModuleProxy.getBean("prototypeScopeBean");

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
			
			singletonBean = this.lifecycleModuleProxy.getBean("singletonScopeBean");
			prototypeBean1 = this.lifecycleModuleProxy.getBean("prototypeScopeBean");
			prototypeBean2 = this.lifecycleModuleProxy.getBean("prototypeScopeBean");

			Assertions.assertTrue((boolean)singletonBean.getClass().getField("beanInjected").get(singletonBean));
			Assertions.assertTrue((boolean)prototypeBean1.getClass().getField("beanInjected").get(prototypeBean1));
			Assertions.assertTrue((boolean)prototypeBean2.getClass().getField("beanInjected").get(prototypeBean2));
		} 
		finally {
			moduleA.stop();
		}
	}
	
	@Test
	public void testDestroyWithDI() throws IllegalArgumentException, IllegalAccessException, NoSuchFieldException, SecurityException {
		
	}
}
