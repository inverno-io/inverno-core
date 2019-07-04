package io.winterframework.test;

import java.io.File;
import java.io.IOException;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import io.winterframework.core.test.AbstractWinterTest;
import io.winterframework.core.test.WinterCompilationException;
import io.winterframework.core.test.WinterCompiler;
import io.winterframework.core.test.WinterModuleLoader;
import io.winterframework.core.test.WinterModuleProxy;

public class TestMultiModule extends AbstractWinterTest {

	private static final String MODULEA_MODULE = "io.winterframework.test.multi.moduleA";
	private static final String MODULEB_MODULE = "io.winterframework.test.multi.moduleB";
	private static final String MODULEC_MODULE = "io.winterframework.test.multi.moduleC";
	private static final String MODULED_MODULE = "io.winterframework.test.multi.moduleD";
	private static final String MODULEE_MODULE = "io.winterframework.test.multi.moduleE";
	private static final String MODULEF_MODULE = "io.winterframework.test.multi.moduleF";

	@Test
	public void testMultiModuleSimple() throws IOException, WinterCompilationException, IllegalArgumentException, IllegalAccessException, NoSuchFieldException, SecurityException {
		WinterModuleLoader moduleLoader = this.getWinterCompiler().compile(MODULEA_MODULE, MODULEB_MODULE);
		
		WinterModuleProxy moduleA = moduleLoader.load(MODULEA_MODULE).build();
		moduleA.start();
		try {
			Object beanA = moduleA.getBean("beanA");
			Assertions.assertNotNull(beanA);
		}
		finally {
			moduleA.stop();
		}
		
		WinterModuleProxy moduleB = moduleLoader.load(MODULEB_MODULE).build();
		moduleB.start();
		try {
			Object beanB = moduleB.getBean("beanB");
			Assertions.assertNotNull(beanB);
			
			Object beanB_beanA = beanB.getClass().getField("beanA").get(beanB);
			Assertions.assertNotNull(beanB_beanA);
		}
		finally {
			moduleB.stop();
		}
	}
	
	@Test
	public void testMultiModuleImport() throws IOException, WinterCompilationException, IllegalArgumentException, IllegalAccessException, NoSuchFieldException, SecurityException {
		this.getWinterCompiler().compile(MODULEA_MODULE, MODULEB_MODULE);
		
		WinterCompiler extraCompiler = new WinterCompiler(new File(WINTER_CORE), 
				new File(WINTER_CORE_ANNOTATION), 
				new File(WINTER_CORE_COMPILER), 
				new File(MODULE_SOURCE), 
				new File(MODULE_SOURCE_TARGET),
				new File(MODULE_TARGET),
				new File[] {new File(MODULE_TARGET, MODULEA_MODULE), new File(MODULE_TARGET, MODULEB_MODULE)});
		
		WinterModuleLoader moduleLoader = extraCompiler.compile(MODULEC_MODULE);
		
		WinterModuleProxy moduleC = moduleLoader.load(MODULEC_MODULE).build();
		moduleC.start();
		try {
			Object beanC = moduleC.getBean("beanC");
			Assertions.assertNotNull(beanC);
			
			Object beanC_beanA = beanC.getClass().getField("beanA").get(beanC);
			Assertions.assertNotNull(beanC_beanA);
			Object beanC_beanB = beanC.getClass().getField("beanB").get(beanC);
			Assertions.assertNotNull(beanC_beanB);
			
			Object beanC_beanB_beanA = beanC_beanB.getClass().getField("beanA").get(beanC_beanB);
			Assertions.assertNotNull(beanC_beanB_beanA);
			Assertions.assertNotEquals(beanC_beanA, beanC_beanB_beanA);
		}
		finally {
			moduleC.stop();
		}
	}
	
	@Test
	public void testMultiModuleSocket() throws IOException, WinterCompilationException, IllegalArgumentException, IllegalAccessException, NoSuchFieldException, SecurityException {
		WinterModuleLoader moduleLoader = this.getWinterCompiler().compile(MODULEA_MODULE, MODULED_MODULE, MODULEE_MODULE, MODULEF_MODULE);
		
		WinterModuleProxy moduleD = moduleLoader.load(MODULED_MODULE).build();
		moduleD.start();
		try {
			Object beanD = moduleD.getBean("beanD");
			Assertions.assertNotNull(beanD);
			
			Object beanD_beanE = beanD.getClass().getField("beanE").get(beanD);
			Assertions.assertNotNull(beanD_beanE);
			
			Object beanD_beanE_beanA = beanD_beanE.getClass().getField("beanA").get(beanD_beanE);
			Assertions.assertNotNull(beanD_beanE_beanA);
			
			Object beanD_beanF = beanD.getClass().getField("beanF").get(beanD);
			Assertions.assertNotNull(beanD_beanF);
			
			Object beanD_beanF_beanA = beanD_beanE.getClass().getField("beanA").get(beanD_beanE);
			Assertions.assertNotNull(beanD_beanF_beanA);
		}
		finally {
			moduleD.stop();
		}
	}
}