package io.winterframework.test;

import java.io.IOException;
import java.util.Locale;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import io.winterframework.core.test.AbstractWinterTest;
import io.winterframework.core.test.WinterCompilationException;
import io.winterframework.core.test.WinterModuleLoader;
import io.winterframework.core.test.WinterModuleProxy;

public class TestProvide extends AbstractWinterTest {

	private static final String MODULEA = "io.winterframework.test.provide.moduleA";
	private static final String MODULEB = "io.winterframework.test.provide.moduleB";
	private static final String MODULEC = "io.winterframework.test.provide.moduleC";
	
	@Test
	public void testProvideInternalWiring() throws IOException, WinterCompilationException, IllegalArgumentException, IllegalAccessException, NoSuchFieldException, SecurityException {
		WinterModuleLoader moduleLoader = this.getWinterCompiler().compile(MODULEA);
		
		WinterModuleProxy moduleA = moduleLoader.load(MODULEA).build();
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
		WinterModuleLoader moduleLoader = this.getWinterCompiler().compile(MODULEA, MODULEB);
		
		WinterModuleProxy moduleB = moduleLoader.load(MODULEB).build();
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
	
	@Test
	public void testProvideMultipleError() throws IOException {
		try {
			this.getWinterCompiler().compile(MODULEC);
			Assertions.fail("Should throw a WinterCompilationException");
		}
		catch(WinterCompilationException e) {
			Assertions.assertEquals(1, e.getDiagnotics().size());
			Assertions.assertEquals("Bean io.winterframework.test.provide.moduleC:beanA can't provide multiple types", e.getDiagnotics().get(0).getMessage(Locale.getDefault()));
		}
	}
}
