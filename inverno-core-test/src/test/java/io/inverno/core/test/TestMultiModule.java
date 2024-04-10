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
import io.inverno.test.InvernoTestCompiler;
import io.inverno.test.InvernoModuleLoader;
import io.inverno.test.InvernoModuleProxy;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 
 * @author <a href="mailto:jeremy.kuhn@inverno.io">Jeremy Kuhn</a>
 *
 */
public class TestMultiModule extends AbstractCoreInvernoTest {

	private static final String MODULEA = "io.inverno.core.test.multi.moduleA";
	private static final String MODULEB = "io.inverno.core.test.multi.moduleB";
	private static final String MODULEC = "io.inverno.core.test.multi.moduleC";
	private static final String MODULED = "io.inverno.core.test.multi.moduleD";
	private static final String MODULEE = "io.inverno.core.test.multi.moduleE";
	private static final String MODULEF = "io.inverno.core.test.multi.moduleF";
	private static final String MODULEG = "io.inverno.core.test.multi.moduleG";
	private static final String MODULEH = "io.inverno.core.test.multi.moduleH";
	private static final String MODULEI = "io.inverno.core.test.multi.moduleI";
	private static final String MODULEJ = "io.inverno.core.test.multi.moduleJ";
	private static final String MODULEK = "io.inverno.core.test.multi.moduleK";
	private static final String MODULEL = "io.inverno.core.test.multi.moduleL";

	@Test
	public void testMultiModuleSimple() throws IOException, InvernoCompilationException, IllegalArgumentException, IllegalAccessException, NoSuchFieldException, SecurityException {
		InvernoModuleLoader moduleLoader = this.getInvernoCompiler().compile(MODULEA, MODULEB);
		
		InvernoModuleProxy moduleA = moduleLoader.load(MODULEA).build();
		moduleA.start();
		try {
			Object beanA = moduleA.getBean("beanA");
			Assertions.assertNotNull(beanA);
		}
		finally {
			moduleA.stop();
		}
		
		InvernoModuleProxy moduleB = moduleLoader.load(MODULEB).build();
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
	public void testMultiModuleImport() throws IOException, InvernoCompilationException, IllegalArgumentException, IllegalAccessException, NoSuchFieldException, SecurityException {
		this.getInvernoCompiler().compile(MODULEA, MODULEB);
		
		InvernoTestCompiler extraCompiler = this.getInvernoCompiler().withModulePaths(Set.of(this.getInvernoCompiler().getModuleOutputPath().resolve(MODULEA), this.getInvernoCompiler().getModuleOutputPath().resolve(MODULEB)));
		InvernoModuleLoader moduleLoader = extraCompiler.compile(MODULEC);
		InvernoModuleProxy moduleC = moduleLoader.load(MODULEC).build();
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
	public void testMultiModuleSocket() throws IOException, InvernoCompilationException, IllegalArgumentException, IllegalAccessException, NoSuchFieldException, SecurityException {
		InvernoModuleLoader moduleLoader = this.getInvernoCompiler().compile(MODULEA, MODULED, MODULEE, MODULEF);
		
		InvernoModuleProxy moduleD = moduleLoader.load(MODULED).build();
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
	
	@Test
	public void testComponentModuleBeanExclusionWiring() throws IOException, InvernoCompilationException {
		InvernoModuleLoader moduleLoader = this.getInvernoCompiler().compile(MODULEG, MODULEH);
		moduleLoader.load(MODULEH).build();
	}
	
	@Test
	public void testEmptyComponentModule() throws IOException, InvernoCompilationException {
		InvernoModuleLoader moduleLoader = this.getInvernoCompiler().compile(MODULEI, MODULEJ);
		
		InvernoModuleProxy moduleI = moduleLoader.load(MODULEI).build();
		moduleI.start();
		moduleI.stop();
		
		this.getInvernoCompiler().cleanModuleTarget(MODULEI, MODULEJ);
		
		this.getInvernoCompiler().compile(MODULEJ);
		
		InvernoTestCompiler extraCompiler = this.getInvernoCompiler().withModulePaths(Set.of(this.getInvernoCompiler().getModuleOutputPath().resolve(MODULEJ)));
		moduleLoader = extraCompiler.compile(MODULEI);
		
		moduleI = moduleLoader.load(MODULEI).build();
		moduleI.start();
		moduleI.stop();
	}
	
	@Test
	public void testMultiSocketOrdering() throws IOException, InvernoCompilationException, NoSuchFieldException, IllegalArgumentException, IllegalAccessException {
		InvernoModuleLoader moduleLoader = this.getInvernoCompiler().compile(MODULEK, MODULEL);
		
		List<Runnable> extRunnables = List.of(new ExtRunnable1(), new ExtRunnable2());
		
		InvernoModuleProxy moduleK = moduleLoader.load(MODULEK).dependencies(extRunnables).build();
		moduleK.start();
		try {
			Object runnableService = moduleK.getBean("runnableService");
			Assertions.assertNotNull(runnableService);
			
			Object runnableService_runnablesList = runnableService.getClass().getField("runnables").get(runnableService);
			
			Assertions.assertNotNull(runnableService_runnablesList);
			Assertions.assertEquals(6, ((List)runnableService_runnablesList).size());
			
			List<String> runnableClassNames = ((List<Object>)runnableService_runnablesList).stream().map(o -> o.getClass().getSimpleName()).collect(Collectors.toList());

			// 1. Socket beans
			Assertions.assertEquals(Set.of("ExtRunnable1", "ExtRunnable2"), runnableClassNames.subList(0, 2).stream().collect(Collectors.toSet()));
			// 2. module beans
			Assertions.assertEquals(Set.of("ModKRunnable1", "ModKRunnable2"), runnableClassNames.subList(2, 4).stream().collect(Collectors.toSet()));
			// 3. nested module beans
			Assertions.assertEquals(Set.of("ModLRunnable1", "ModLRunnable2"), runnableClassNames.subList(4, 6).stream().collect(Collectors.toSet()));
		}
		finally {
			moduleK.stop();
		}
	}
	
	private static class ExtRunnable1 implements Runnable {

		@Override
		public void run() {}
	}
	
	private static class ExtRunnable2 implements Runnable {

		@Override
		public void run() {}
	}
}