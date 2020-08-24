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

/**
 * @author jkuhn
 *
 */
public class TestMultiCycle extends AbstractWinterTest {

	private static final String MODULEAPI = "io.winterframework.test.multicycle.moduleAPI";
	
	private static final String MODULEA = "io.winterframework.test.multicycle.moduleA";
	private static final String MODULEB = "io.winterframework.test.multicycle.moduleB";
	
	private static final String MODULEC = "io.winterframework.test.multicycle.moduleC";
	private static final String MODULED = "io.winterframework.test.multicycle.moduleD";
	private static final String MODULEE = "io.winterframework.test.multicycle.moduleE";
	private static final String MODULEF = "io.winterframework.test.multicycle.moduleF";

	@Test
	public void testMultiCycleSimple() throws IOException {
		this.clearModuleTarget();
		try {
			this.getWinterCompiler().compile(MODULEA, MODULEB);
			Assertions.fail("Should throw a WinterCompilationException");
		}
		catch(WinterCompilationException e) {
			Assertions.assertEquals(1, e.getDiagnostics().size());
			
			String cycleMessage = "Bean io.winterframework.test.multicycle.moduleA:beanA forms a cycle in module io.winterframework.test.multicycle.moduleA\n" + 
				"  ┌────────────────────────────┐\n" + 
				"  │                            │\n" + 
				"  │    io.winterframework.test.multicycle.moduleA:beanA\n" + 
				"  │                            │\n" + 
				"  │                            │ io.winterframework.test.multicycle.moduleA:beanA:callable\n" + 
				"  │                            │\n" + 
				"  │                            ▼\n" + 
				"  │    io.winterframework.test.multicycle.moduleB:beanA\n" + 
				"  ▲                            │\n" + 
				"  │                            │ io.winterframework.test.multicycle.moduleB:beanA:runnable\n" + 
				"  │                            │\n" + 
				"  │                            ▼\n" + 
				"  │                            ┊\n" + 
				"  │                            ┊ io.winterframework.test.multicycle.moduleB:runnableSocket\n" + 
				"  │                            ┊\n" + 
				"  └────────────────────────────┘ "; 
			
			Assertions.assertEquals(cycleMessage, e.getDiagnostics().get(0).getMessage(Locale.getDefault()));
		}
	}
	
	@Test
	public void testMultiCycleComplex() throws IOException, WinterCompilationException {
		this.clearModuleTarget();
		try {
			this.getWinterCompiler().compile(MODULEAPI, MODULED, MODULEE, MODULEF, MODULEC);
			Assertions.fail("Should throw a WinterCompilationException");
		}
		catch(WinterCompilationException e) {
			Assertions.assertEquals(2, e.getDiagnostics().size());
			
			String cycleMessage = "Bean io.winterframework.test.multicycle.moduleC:serviceCImpl forms a cycle in module io.winterframework.test.multicycle.moduleC\n" + 
				"  ┌───────────────────────────────┐\n" + 
				"  │                               │\n" + 
				"  │    io.winterframework.test.multicycle.moduleC:serviceCImpl\n" + 
				"  │                               │\n" + 
				"  │                               │ io.winterframework.test.multicycle.moduleC:serviceCImpl:serviceD\n" + 
				"  │                               │\n" + 
				"  │                               ▼\n" + 
				"  │    io.winterframework.test.multicycle.moduleD:serviceDImpl\n" + 
				"  │                               │\n" + 
				"  │                               │ io.winterframework.test.multicycle.moduleD:serviceDImpl:beanA\n" + 
				"  │                               │\n" + 
				"  │                               ▼\n" + 
				"  │       io.winterframework.test.multicycle.moduleD:beanA\n" + 
				"  │                               │\n" + 
				"  │                               │ io.winterframework.test.multicycle.moduleD:beanA:serviceE\n" + 
				"  │                               │\n" + 
				"  │                               ▼\n" + 
				"  │                               ┊\n" + 
				"  │                               ┊ io.winterframework.test.multicycle.moduleD:serviceESocket\n" + 
				"  ▲                               ┊\n" + 
				"  │                               ▼\n" + 
				"  │    io.winterframework.test.multicycle.moduleE:serviceEImpl\n" + 
				"  │                               │\n" + 
				"  │                               │ io.winterframework.test.multicycle.moduleE:serviceEImpl:serviceF\n" + 
				"  │                               │\n" + 
				"  │                               ▼\n" + 
				"  │    io.winterframework.test.multicycle.moduleF:serviceFImpl\n" + 
				"  │                               │\n" + 
				"  │                               │ io.winterframework.test.multicycle.moduleF:serviceFImpl:serviceC\n" + 
				"  │                               │\n" + 
				"  │                               ▼\n" + 
				"  │                               ┊\n" + 
				"  │                               ┊ io.winterframework.test.multicycle.moduleF:serviceCSocket\n" + 
				"  │                               ┊\n" + 
				"  │                               ▼\n" + 
				"  │                               ┊\n" + 
				"  │                               ┊ io.winterframework.test.multicycle.moduleE:serviceCSocket\n" + 
				"  │                               ┊\n" + 
				"  └───────────────────────────────┘ "; 
			
			Assertions.assertEquals(cycleMessage, e.getDiagnostics().get(0).getMessage(Locale.getDefault()));
		}
	}
	
	@Test
	public void testMultiCycleSimpleImport() throws IOException, WinterCompilationException {
		this.clearModuleTarget();
		try {
			this.getWinterCompiler().compile(MODULEB);
		
			WinterCompiler extraCompiler = new WinterCompiler(new File(WINTER_CORE), 
				new File(WINTER_CORE_ANNOTATION), 
				new File(WINTER_CORE_COMPILER), 
				new File(WINTER_EXTERNAL_DEPENDENCIES),
				new File(MODULE_SOURCE), 
				new File(MODULE_SOURCE_TARGET),
				new File(MODULE_TARGET),
				new File[] {new File(MODULE_TARGET, MODULEA)});
		
			extraCompiler.compile(MODULEA);
		}
		catch(WinterCompilationException e) {
			Assertions.assertEquals(1, e.getDiagnostics().size());
			
			String cycleMessage = "Bean io.winterframework.test.multicycle.moduleA:beanA forms a cycle in module io.winterframework.test.multicycle.moduleA\n" + 
				"  ┌────────────────────────────┐\n" + 
				"  │                            │\n" + 
				"  │    io.winterframework.test.multicycle.moduleA:beanA\n" + 
				"  │                            │\n" + 
				"  │                            │ io.winterframework.test.multicycle.moduleA:beanA:callable\n" + 
				"  │                            │\n" + 
				"  ▲                            ▼\n" + 
				"  │    io.winterframework.test.multicycle.moduleB:beanA\n" + 
				"  │                            ┊\n" + 
				"  │                           (┄) io.winterframework.test.multicycle.moduleB:runnableSocket\n" + 
				"  │                            ┊\n" + 
				"  └────────────────────────────┘ "; 
			
			Assertions.assertEquals(cycleMessage, e.getDiagnostics().get(0).getMessage(Locale.getDefault()));
		}		
	}
}
