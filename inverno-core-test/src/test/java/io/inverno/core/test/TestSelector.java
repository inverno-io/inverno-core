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
package io.inverno.core.test;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Locale;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import io.inverno.test.InvernoCompilationException;
import io.inverno.test.InvernoTestCompiler;
import io.inverno.test.InvernoModuleLoader;
import io.inverno.test.InvernoModuleProxy;

/**
 * 
 * @author <a href="mailto:jeremy.kuhn@inverno.io">Jeremy Kuhn</a>
 *
 */
public class TestSelector extends AbstractCoreInvernoTest {

	private static final String MODULEA = "io.inverno.core.test.selector.moduleA";
	private static final String MODULEB = "io.inverno.core.test.selector.moduleB";
	private static final String MODULEC = "io.inverno.core.test.selector.moduleC";
	private static final String MODULED = "io.inverno.core.test.selector.moduleD";
	
	@Test
	public void testSelectorBeanSocket() throws IOException, InvernoCompilationException, IllegalArgumentException, IllegalAccessException, NoSuchFieldException, SecurityException {
		this.clearModuleTarget();
		InvernoModuleLoader moduleLoader = this.getInvernoCompiler().compile(MODULEA);
		
		InvernoModuleProxy moduleA = moduleLoader.load(MODULEA).build();
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
	public void testSelectorSocketBean() throws IOException, InvernoCompilationException, IllegalArgumentException, IllegalAccessException, NoSuchFieldException, SecurityException {
		this.clearModuleTarget();
		InvernoModuleLoader moduleLoader = this.getInvernoCompiler().compile(MODULEB, MODULEC);
		
		InvernoModuleProxy moduleC = moduleLoader.load(MODULEC).build();
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
	public void testSelectorSocketBeanComponent() throws IOException, InvernoCompilationException, IllegalArgumentException, IllegalAccessException, NoSuchFieldException, SecurityException {
		this.clearModuleTarget();
		this.getInvernoCompiler().compile(MODULEB);
		
		InvernoTestCompiler extraCompiler = this.getInvernoCompiler().withModulePaths(List.of(new File(this.getInvernoCompiler().getModuleOutputPath(), MODULEC)));
		InvernoModuleLoader moduleLoader = extraCompiler.compile(MODULEC);
		InvernoModuleProxy moduleC = moduleLoader.load(MODULEC).build();
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
	public void testSelectorSocketBeanBadExplicit() throws IOException, InvernoCompilationException, IllegalArgumentException, IllegalAccessException, NoSuchFieldException, SecurityException {
		this.clearModuleTarget();
		try {
			this.getInvernoCompiler().compile(MODULEB, MODULED);
			Assertions.fail("Should throw an InvernoCompilationException");
		}
		catch(InvernoCompilationException e) {
			Assertions.assertEquals(1, e.getDiagnostics().size());
			System.out.println(e.getDiagnostics().get(0).getMessage(Locale.getDefault()));
			Assertions.assertEquals("Bean io.inverno.core.test.selector.moduleD:runnableB of type io.inverno.core.test.selector.moduleD.RunnableB is not wirable into socket io.inverno.core.test.selector.moduleB:runnableSocket of type java.lang.Runnable", e.getDiagnostics().get(0).getMessage(Locale.getDefault()));
		}
	}
}
