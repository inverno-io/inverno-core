package io.winterframework.test;

import java.io.IOException;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import io.winterframework.core.test.AbstractWinterTest;
import io.winterframework.core.test.WinterCompilationException;
import io.winterframework.core.test.WinterModuleLoader;
import io.winterframework.core.test.WinterModuleProxy;

public class TestIncludeExclude extends AbstractWinterTest {

	private static final String MODULEA = "io.winterframework.test.includeexclude.moduleA";
	private static final String MODULEB = "io.winterframework.test.includeexclude.moduleB";
	private static final String MODULEC = "io.winterframework.test.includeexclude.moduleC";
	private static final String MODULED = "io.winterframework.test.includeexclude.moduleD";
	private static final String MODULEE = "io.winterframework.test.includeexclude.moduleE";

	@Test
	public void testIncludes() throws IOException, WinterCompilationException, IllegalArgumentException, IllegalAccessException, NoSuchFieldException, SecurityException {
		WinterModuleLoader moduleLoader = this.getWinterCompiler().compile(MODULEA, MODULEB, MODULEC);
		
		WinterModuleProxy moduleC = moduleLoader.load(MODULEC).build();
		moduleC.start();
		try {
			Object beanC = moduleC.getBean("beanC");
			Assertions.assertNotNull(beanC);
			
			Object beanC_runnable = beanC.getClass().getField("runnable").get(beanC);
			Assertions.assertNotNull(beanC_runnable);
			
			Object beanC_callable = beanC.getClass().getField("callable").get(beanC);
			Assertions.assertNull(beanC_callable);
			
		}
		finally {
			moduleC.stop();
		}
	}
	
	@Test
	public void testExcludes() throws IOException, WinterCompilationException, IllegalArgumentException, IllegalAccessException, NoSuchFieldException, SecurityException {
		WinterModuleLoader moduleLoader = this.getWinterCompiler().compile(MODULEA, MODULEB, MODULED);
		
		WinterModuleProxy moduleD = moduleLoader.load(MODULED).build();
		moduleD.start();
		try {
			Object beanD = moduleD.getBean("beanD");
			Assertions.assertNotNull(beanD);
			
			Object beanD_runnable = beanD.getClass().getField("runnable").get(beanD);
			Assertions.assertNull(beanD_runnable);
			
			Object beanD_callable = beanD.getClass().getField("callable").get(beanD);
			Assertions.assertNotNull(beanD_callable);
			
		}
		finally {
			moduleD.stop();
		}
	}
	
	@Test
	public void testIncludesExcludes() throws IOException, WinterCompilationException, IllegalArgumentException, IllegalAccessException, NoSuchFieldException, SecurityException {
		WinterModuleLoader moduleLoader = this.getWinterCompiler().compile(MODULEA, MODULEB, MODULEE);
		
		WinterModuleProxy moduleE = moduleLoader.load(MODULEE).build();
		moduleE.start();
		try {
			Object beanE = moduleE.getBean("beanE");
			Assertions.assertNotNull(beanE);
			
			Object beanE_runnable = beanE.getClass().getField("runnable").get(beanE);
			Assertions.assertNull(beanE_runnable);
			
			Object beanE_callable = beanE.getClass().getField("callable").get(beanE);
			Assertions.assertNotNull(beanE_callable);
			
		}
		finally {
			moduleE.stop();
		}
	}
	
}