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

import java.io.File;
import java.io.IOException;
import java.util.Locale;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import io.winterframework.core.test.AbstractWinterTest;
import io.winterframework.core.test.WinterCompilationException;
import io.winterframework.core.test.WinterCompiler;
import io.winterframework.core.test.WinterModuleLoader;
import io.winterframework.core.test.WinterModuleProxy;

/**
 * 
 * @author jkuhn
 *
 */
public class TestSelector extends AbstractWinterTest {

	private static final String MODULEA = "io.winterframework.test.selector.moduleA";
	private static final String MODULEB = "io.winterframework.test.selector.moduleB";
	private static final String MODULEC = "io.winterframework.test.selector.moduleC";
	private static final String MODULED = "io.winterframework.test.selector.moduleD";
	
	@Test
	public void testSelectorBeanSocket() throws IOException, WinterCompilationException, IllegalArgumentException, IllegalAccessException, NoSuchFieldException, SecurityException {
		WinterModuleLoader moduleLoader = this.getWinterCompiler().compile(MODULEA);
		
		WinterModuleProxy moduleA = moduleLoader.load(MODULEA).build();
		moduleA.start();
		try {
			Object beanA = moduleA.getBean("beanA");
			Assertions.assertNotNull(beanA);
			
			Object runnableA = moduleA.getBean("runnableA");
			Assertions.assertNotNull(runnableA);
			
			Object runnableB = moduleA.getBean("runnableB");
			Assertions.assertNotNull(runnableB);
			
			Object beanA_runnable = beanA.getClass().getField("runnable").get(beanA);
			Assertions.assertNotNull(beanA_runnable);
			
			Assertions.assertEquals(runnableA, beanA_runnable);
		}
		finally {
			moduleA.stop();
		}
	}
	
	@Test
	public void testSelectorSocketBean() throws IOException, WinterCompilationException, IllegalArgumentException, IllegalAccessException, NoSuchFieldException, SecurityException {
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
			
			Object beanC_beanB_runnable = beanC_beanB.getClass().getField("runnable").get(beanC_beanB);
			
			Assertions.assertEquals(runnableA, beanC_beanB_runnable);
		}
		finally {
			moduleC.stop();
		}
	}
	
	@Test
	public void testSelectorSocketBeanComponent() throws IOException, WinterCompilationException, IllegalArgumentException, IllegalAccessException, NoSuchFieldException, SecurityException {
		this.clearModuleTarget();
		this.getWinterCompiler().compile(MODULEB);
		
		WinterCompiler extraCompiler = new WinterCompiler(new File(WINTER_CORE), 
			new File(WINTER_CORE_ANNOTATION), 
			new File(WINTER_CORE_COMPILER), 
			new File(WINTER_EXTERNAL_DEPENDENCIES),
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
			
			Object beanC_beanB_runnable = beanC_beanB.getClass().getField("runnable").get(beanC_beanB);
			
			Assertions.assertEquals(runnableA, beanC_beanB_runnable);
		}
		finally {
			moduleC.stop();
		}
	}
	
	@Test
	public void testSelectorSocketBeanBadExplicit() throws IOException, WinterCompilationException, IllegalArgumentException, IllegalAccessException, NoSuchFieldException, SecurityException {
		try {
			this.getWinterCompiler().compile(MODULEB, MODULED);
			Assertions.fail("Should throw a WinterCompilationException");
		}
		catch(WinterCompilationException e) {
			Assertions.assertEquals(1, e.getDiagnostics().size());
			System.out.println(e.getDiagnostics().get(0).getMessage(Locale.getDefault()));
			Assertions.assertEquals("Bean io.winterframework.test.selector.moduleD:runnableB of type io.winterframework.test.selector.moduleD.RunnableB is not wirable into socket io.winterframework.test.selector.moduleB:runnableSocket of type java.lang.Runnable", e.getDiagnostics().get(0).getMessage(Locale.getDefault()));
		}
	}
}
