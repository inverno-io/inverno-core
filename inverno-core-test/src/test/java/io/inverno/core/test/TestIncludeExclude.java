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

import java.io.IOException;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import io.inverno.test.InvernoCompilationException;
import io.inverno.test.InvernoModuleLoader;
import io.inverno.test.InvernoModuleProxy;

/**
 * 
 * @author <a href="mailto:jeremy.kuhn@inverno.io">Jeremy Kuhn</a>
 *
 */
public class TestIncludeExclude extends AbstractCoreInvernoTest {

	private static final String MODULEA = "io.inverno.core.test.includeexclude.moduleA";
	private static final String MODULEB = "io.inverno.core.test.includeexclude.moduleB";
	private static final String MODULEC = "io.inverno.core.test.includeexclude.moduleC";
	private static final String MODULED = "io.inverno.core.test.includeexclude.moduleD";
	private static final String MODULEE = "io.inverno.core.test.includeexclude.moduleE";

	@Test
	public void testIncludes() throws IOException, InvernoCompilationException, IllegalArgumentException, IllegalAccessException, NoSuchFieldException, SecurityException {
		InvernoModuleLoader moduleLoader = this.getInvernoCompiler().compile(MODULEA, MODULEB, MODULEC);
		
		InvernoModuleProxy moduleC = moduleLoader.load(MODULEC).build();
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
	public void testExcludes() throws IOException, InvernoCompilationException, IllegalArgumentException, IllegalAccessException, NoSuchFieldException, SecurityException {
		InvernoModuleLoader moduleLoader = this.getInvernoCompiler().compile(MODULEA, MODULEB, MODULED);
		
		InvernoModuleProxy moduleD = moduleLoader.load(MODULED).build();
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
	public void testIncludesExcludes() throws IOException, InvernoCompilationException, IllegalArgumentException, IllegalAccessException, NoSuchFieldException, SecurityException {
		InvernoModuleLoader moduleLoader = this.getInvernoCompiler().compile(MODULEA, MODULEB, MODULEE);
		
		InvernoModuleProxy moduleE = moduleLoader.load(MODULEE).build();
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