package io.winterframework.test;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import io.winterframework.core.test.AbstractWinterTest;
import io.winterframework.core.test.WinterCompilationException;
import io.winterframework.core.test.WinterCompiler;
import io.winterframework.core.test.WinterModuleLoader;
import io.winterframework.core.test.WinterModuleProxy;

public class TestMultiSocketBean extends AbstractWinterTest {
	
	private static final String MODULEB = "io.winterframework.test.socketbean.moduleB";
	private static final String MODULEC = "io.winterframework.test.socketbean.moduleC";
	
	@SuppressWarnings("unchecked")
	@Test
	public void testMultiSocketBean() throws IOException, WinterCompilationException, IllegalArgumentException, IllegalAccessException, NoSuchFieldException, SecurityException {
		this.clearModuleTarget();
		WinterModuleLoader moduleLoader = this.getWinterCompiler().compile(MODULEB, MODULEC);
		
		WinterModuleProxy moduleC = moduleLoader.load(MODULEC).build();
		moduleC.start();
		try {
			Object beanC = moduleC.getBean("beanC");
			Assertions.assertNotNull(beanC);
			
			Object runnableA = moduleC.getBean("runnableA");
			Assertions.assertNotNull(runnableA);
			
			Object runnableB = moduleC.getBean("runnableB");
			Assertions.assertNotNull(runnableB);
			
			Object beanC_beanB = beanC.getClass().getField("beanB").get(beanC);
			Assertions.assertNotNull(beanC_beanB);
			
			List<Runnable> beanC_beanB_runnables = (List<Runnable>)beanC_beanB.getClass().getField("runnables").get(beanC_beanB);
			
			Assertions.assertTrue(beanC_beanB_runnables.containsAll(List.of(runnableA, runnableB)));
		}
		finally {
			moduleC.stop();
		}
	}
	
	@SuppressWarnings("unchecked")
	@Test
	public void testMultiSocketBeanImported() throws IOException, WinterCompilationException, IllegalArgumentException, IllegalAccessException, NoSuchFieldException, SecurityException {
		this.clearModuleTarget();
		this.getWinterCompiler().compile(MODULEB);
		
		WinterCompiler extraCompiler = new WinterCompiler(new File(WINTER_CORE), 
			new File(WINTER_CORE_ANNOTATION), 
			new File(WINTER_CORE_COMPILER), 
			new File(MODULE_SOURCE), 
			new File(MODULE_SOURCE_TARGET),
			new File(MODULE_TARGET),
			new File[] {new File(MODULE_TARGET, MODULEC)});
	
		WinterModuleLoader moduleLoader = extraCompiler.compile(MODULEC);
		
		WinterModuleProxy moduleC = moduleLoader.load(MODULEC).build();
		moduleC.start();
		try {
			Object beanC = moduleC.getBean("beanC");
			Assertions.assertNotNull(beanC);
			
			Object runnableA = moduleC.getBean("runnableA");
			Assertions.assertNotNull(runnableA);
			
			Object runnableB = moduleC.getBean("runnableB");
			Assertions.assertNotNull(runnableB);
			
			Object beanC_beanB = beanC.getClass().getField("beanB").get(beanC);
			Assertions.assertNotNull(beanC_beanB);
			
			List<Runnable> beanC_beanB_runnables = (List<Runnable>)beanC_beanB.getClass().getField("runnables").get(beanC_beanB);
			
			Assertions.assertTrue(beanC_beanB_runnables.containsAll(List.of(runnableA, runnableB)));
		}
		finally {
			moduleC.stop();
		}
	}
}
