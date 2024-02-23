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
import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import io.inverno.test.InvernoCompilationException;
import io.inverno.test.InvernoTestCompiler;
import io.inverno.test.InvernoModuleLoader;
import io.inverno.test.InvernoModuleProxy;
import java.util.Set;

/**
 * 
 * @author <a href="mailto:jeremy.kuhn@inverno.io">Jeremy Kuhn</a>
 *
 */
public class TestMultiSocketBean extends AbstractCoreInvernoTest {
	
	private static final String MODULEB = "io.inverno.core.test.socketbean.moduleB";
	private static final String MODULEC = "io.inverno.core.test.socketbean.moduleC";
	
	@SuppressWarnings("unchecked")
	@Test
	public void testMultiSocketBean() throws IOException, InvernoCompilationException, IllegalArgumentException, IllegalAccessException, NoSuchFieldException, SecurityException {
		this.getInvernoCompiler().cleanModuleTarget();
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
			
			List<Runnable> beanC_beanB_runnables = (List<Runnable>)beanC_beanB.getClass().getField("runnables").get(beanC_beanB);
			
			Assertions.assertTrue(beanC_beanB_runnables.containsAll(List.of(runnableA, runnableB)));
		}
		finally {
			moduleC.stop();
		}
	}
	
	@SuppressWarnings("unchecked")
	@Test
	public void testMultiSocketBeanComponent() throws IOException, InvernoCompilationException, IllegalArgumentException, IllegalAccessException, NoSuchFieldException, SecurityException {
		this.getInvernoCompiler().cleanModuleTarget();
		this.getInvernoCompiler().compile(MODULEB);
		
		InvernoTestCompiler extraCompiler = this.getInvernoCompiler().withModulePaths(Set.of(this.getInvernoCompiler().getModuleOutputPath().resolve(MODULEC)));
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
			
			List<Runnable> beanC_beanB_runnables = (List<Runnable>)beanC_beanB.getClass().getField("runnables").get(beanC_beanB);
			
			Assertions.assertTrue(beanC_beanB_runnables.containsAll(List.of(runnableA, runnableB)));
		}
		finally {
			moduleC.stop();
		}
	}
}
