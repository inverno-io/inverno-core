package io.winterframework.test;

import java.io.IOException;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import io.winterframework.core.test.AbstractWinterTest;
import io.winterframework.core.test.WinterCompilationException;
import io.winterframework.core.test.WinterModuleLoader;
import io.winterframework.core.test.WinterModuleProxy;

public class TestProvide extends AbstractWinterTest {

	private static final String MODULEA_MODULE = "io.winterframework.test.provide.moduleA";
	private static final String MODULEB_MODULE = "io.winterframework.test.provide.moduleB";
	
	@Test
	public void testProvideInternalWiring() throws IOException, WinterCompilationException, IllegalArgumentException, IllegalAccessException, NoSuchFieldException, SecurityException {
		WinterModuleLoader moduleLoader = this.getWinterCompiler().compile(MODULEA_MODULE);
		
		WinterModuleProxy moduleA = moduleLoader.load(MODULEA_MODULE).build();
		moduleA.start();
		try {
			Object beanA = moduleA.getBean("beanA");
			Assertions.assertNotNull(beanA);
			
			Object beanB = moduleA.getBean("beanB");
			Assertions.assertNotNull(beanB);
			
			Object beanB_runnable = beanB.getClass().getField("runnable").get(beanB);
			Assertions.assertNotNull(beanB_runnable);
			
			Assertions.assertEquals(beanA, beanB_runnable);
		}
		finally {
			moduleA.stop();
		}
	}
	
	@Test
	public void testProvideExternalWiring() throws IOException, WinterCompilationException, IllegalArgumentException, IllegalAccessException, NoSuchFieldException, SecurityException {
		WinterModuleLoader moduleLoader = this.getWinterCompiler().compile(MODULEA_MODULE, MODULEB_MODULE);
		
		WinterModuleProxy moduleB = moduleLoader.load(MODULEB_MODULE).build();
		moduleB.start();
		try {
			Object beanC = moduleB.getBean("beanC");
			Assertions.assertNotNull(beanC);
			
			Object beanC_callable = beanC.getClass().getField("callable").get(beanC);
			Assertions.assertNotNull(beanC_callable);
			
			Object beanC_runnable = beanC.getClass().getField("runnable").get(beanC);
			Assertions.assertNull(beanC_runnable);
		}
		finally {
			moduleB.stop();
		}
	}
}
