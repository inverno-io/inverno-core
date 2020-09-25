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
import java.util.List;
import java.util.Locale;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import io.winterframework.core.test.WinterCompilationException;
import io.winterframework.core.test.WinterTestCompiler;

/**
 * @author jkuhn
 *
 */
public class TestMultiCycle extends AbstractCoreWinterTest {

	private static final String MODULEAPI = "io.winterframework.test.multicycle.moduleAPI";
	
	private static final String MODULEA = "io.winterframework.test.multicycle.moduleA";
	private static final String MODULEB = "io.winterframework.test.multicycle.moduleB";
	
	private static final String MODULEC = "io.winterframework.test.multicycle.moduleC";
	private static final String MODULED = "io.winterframework.test.multicycle.moduleD";
	private static final String MODULEE = "io.winterframework.test.multicycle.moduleE";
	private static final String MODULEF = "io.winterframework.test.multicycle.moduleF";
	
	private static final String MODULEG = "io.winterframework.test.multicycle.moduleG";
	private static final String MODULEH = "io.winterframework.test.multicycle.moduleH";

	private static final String MODULEI = "io.winterframework.test.multicycle.moduleI";
	private static final String MODULEJ = "io.winterframework.test.multicycle.moduleJ";
	
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
			this.getWinterCompiler().compile(MODULEAPI, MODULEF, MODULED, MODULEE, MODULEC);
			Assertions.fail("Should throw a WinterCompilationException");
		}
		catch(WinterCompilationException e) {
			Assertions.assertEquals(1, e.getDiagnostics().size());
			
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
	public void testMultiCycleSimpleBinary() throws IOException, WinterCompilationException {
		this.clearModuleTarget();
		try {
			this.getWinterCompiler().compile(MODULEB);
		
			WinterTestCompiler extraCompiler = this.getWinterCompiler().withModulePaths(List.of(new File(this.getWinterCompiler().getModuleOutputPath(), MODULEA)));
			
			extraCompiler.compile(MODULEA);
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
				"  ▲                            ▼\n" + 
				"  │    io.winterframework.test.multicycle.moduleB:beanA\n" + 
				"  │                            ┊\n" + 
				"  │                           (┄) io.winterframework.test.multicycle.moduleB:runnableSocket\n" + 
				"  │                            ┊\n" + 
				"  └────────────────────────────┘ "; 
			
			Assertions.assertEquals(cycleMessage, e.getDiagnostics().get(0).getMessage(Locale.getDefault()));
		}		
	}
	
	@Test
	public void testMultiCycleWithNested() throws IOException {
		this.clearModuleTarget();
		try {
			this.getWinterCompiler().compile(MODULEG, MODULEH);
			Assertions.fail("Should throw a WinterCompilationException");
		}
		catch(WinterCompilationException e) {
			Assertions.assertEquals(2, e.getDiagnostics().size());
			
			String cycleMessage1 = "Bean io.winterframework.test.multicycle.moduleH:beanH forms a cycle in module io.winterframework.test.multicycle.moduleH\n" + 
					"  ┌──────────────────────────────────┐\n" + 
					"  │                                  │\n" + 
					"  │          io.winterframework.test.multicycle.moduleH:beanH\n" + 
					"  │                                  │\n" + 
					"  │                                  │ io.winterframework.test.multicycle.moduleH:beanH:beanG\n" + 
					"  │                                  │\n" + 
					"  │                                  ▼\n" + 
					"  │          io.winterframework.test.multicycle.moduleG:beanG\n" + 
					"  │                                  │\n" + 
					"  │                                  │ io.winterframework.test.multicycle.moduleG:beanG:runnable\n" + 
					"  ▲                                  │\n" + 
					"  │                                  ▼\n" + 
					"  │                                  ┊\n" + 
					"  │                                  ┊ io.winterframework.test.multicycle.moduleG:runnableSocket\n" + 
					"  │                                  ┊\n" + 
					"  │                                  ▼\n" + 
					"  │    io.winterframework.test.multicycle.moduleH:beanH.someRunnable\n" + 
					"  │                                  │\n" + 
					"  │                                  │ (nested)\n" + 
					"  │                                  │\n" + 
					"  └──────────────────────────────────┘ ";
			
			Assertions.assertEquals(cycleMessage1, e.getDiagnostics().get(0).getMessage(Locale.getDefault()));
			
			String cycleMessage2 = "Bean io.winterframework.test.multicycle.moduleH:beanH.someRunnable forms a cycle in module io.winterframework.test.multicycle.moduleH\n" + 
					"  ┌──────────────────────────────────┐\n" + 
					"  │                                  │\n" + 
					"  │          io.winterframework.test.multicycle.moduleH:beanH\n" + 
					"  │                                  │\n" + 
					"  │                                  │ io.winterframework.test.multicycle.moduleH:beanH:beanG\n" + 
					"  │                                  │\n" + 
					"  │                                  ▼\n" + 
					"  │          io.winterframework.test.multicycle.moduleG:beanG\n" + 
					"  │                                  │\n" + 
					"  │                                  │ io.winterframework.test.multicycle.moduleG:beanG:runnable\n" + 
					"  ▲                                  │\n" + 
					"  │                                  ▼\n" + 
					"  │                                  ┊\n" + 
					"  │                                  ┊ io.winterframework.test.multicycle.moduleG:runnableSocket\n" + 
					"  │                                  ┊\n" + 
					"  │                                  ▼\n" + 
					"  │    io.winterframework.test.multicycle.moduleH:beanH.someRunnable\n" + 
					"  │                                  │\n" + 
					"  │                                  │ (nested)\n" + 
					"  │                                  │\n" + 
					"  └──────────────────────────────────┘ ";
			
			Assertions.assertEquals(cycleMessage2, e.getDiagnostics().get(1).getMessage(Locale.getDefault()));
		}
	}
	
	@Test
	public void testMultiCycleWithNestedBinary() throws IOException {
		this.clearModuleTarget();
		try {
			this.getWinterCompiler().compile(MODULEG);
		
			WinterTestCompiler extraCompiler = this.getWinterCompiler().withModulePaths(List.of(new File(this.getWinterCompiler().getModuleOutputPath(), MODULEH)));
			extraCompiler.compile(MODULEH);
			Assertions.fail("Should throw a WinterCompilationException");
		}
		catch(WinterCompilationException e) {
			Assertions.assertEquals(2, e.getDiagnostics().size());
			
			String cycleMessage1 = "Bean io.winterframework.test.multicycle.moduleH:beanH forms a cycle in module io.winterframework.test.multicycle.moduleH\n" + 
					"  ┌──────────────────────────────────┐\n" + 
					"  │                                  │\n" + 
					"  │          io.winterframework.test.multicycle.moduleH:beanH\n" + 
					"  │                                  │\n" + 
					"  │                                  │ io.winterframework.test.multicycle.moduleH:beanH:beanG\n" + 
					"  │                                  │\n" + 
					"  │                                  ▼\n" + 
					"  │          io.winterframework.test.multicycle.moduleG:beanG\n" + 
					"  ▲                                  ┊\n" + 
					"  │                                 (┄) io.winterframework.test.multicycle.moduleG:runnableSocket\n" + 
					"  │                                  ┊\n" + 
					"  │                                  ▼\n" + 
					"  │    io.winterframework.test.multicycle.moduleH:beanH.someRunnable\n" + 
					"  │                                  │\n" + 
					"  │                                  │ (nested)\n" + 
					"  │                                  │\n" + 
					"  └──────────────────────────────────┘ ";
			
			Assertions.assertEquals(cycleMessage1, e.getDiagnostics().get(0).getMessage(Locale.getDefault()));
			
			String cycleMessage2 = "Bean io.winterframework.test.multicycle.moduleH:beanH.someRunnable forms a cycle in module io.winterframework.test.multicycle.moduleH\n" + 
					"  ┌──────────────────────────────────┐\n" + 
					"  │                                  │\n" + 
					"  │          io.winterframework.test.multicycle.moduleH:beanH\n" + 
					"  │                                  │\n" + 
					"  │                                  │ io.winterframework.test.multicycle.moduleH:beanH:beanG\n" + 
					"  │                                  │\n" + 
					"  │                                  ▼\n" + 
					"  │          io.winterframework.test.multicycle.moduleG:beanG\n" + 
					"  ▲                                  ┊\n" + 
					"  │                                 (┄) io.winterframework.test.multicycle.moduleG:runnableSocket\n" + 
					"  │                                  ┊\n" + 
					"  │                                  ▼\n" + 
					"  │    io.winterframework.test.multicycle.moduleH:beanH.someRunnable\n" + 
					"  │                                  │\n" + 
					"  │                                  │ (nested)\n" + 
					"  │                                  │\n" + 
					"  └──────────────────────────────────┘ ";
			
			Assertions.assertEquals(cycleMessage2, e.getDiagnostics().get(1).getMessage(Locale.getDefault()));
		}
	}
	
	@Test
	public void testMultiCycleWithOverridable() throws IOException {
		this.clearModuleTarget();
		try {
			this.getWinterCompiler().compile(MODULEI, MODULEJ);
			Assertions.fail("Should throw a WinterCompilationException");
		}
		catch(WinterCompilationException e) {
			Assertions.assertEquals(1, e.getDiagnostics().size());
			
			String cycleMessage1 = "Bean io.winterframework.test.multicycle.moduleJ:beanIOverride forms a cycle in module io.winterframework.test.multicycle.moduleJ\n" + 
					"  ┌────────────────────────────────┐\n" + 
					"  │                                │\n" + 
					"  │    io.winterframework.test.multicycle.moduleJ:beanIOverride\n" + 
					"  │                                │\n" + 
					"  │                                │ io.winterframework.test.multicycle.moduleJ:beanIOverride:runnable\n" + 
					"  │                                │\n" + 
					"  │                                ▼\n" + 
					"  │     io.winterframework.test.multicycle.moduleI:someRunnable\n" + 
					"  ▲                                │\n" + 
					"  │                                │ io.winterframework.test.multicycle.moduleI:someRunnable:beanI\n" + 
					"  │                                │\n" + 
					"  │                                ▼\n" + 
					"  │                                ┊\n" + 
					"  │                                ┊ io.winterframework.test.multicycle.moduleI:beanI\n" + 
					"  │                                ┊\n" + 
					"  └────────────────────────────────┘ ";
			
			Assertions.assertEquals(cycleMessage1, e.getDiagnostics().get(0).getMessage(Locale.getDefault()));
		}
	}
	
	@Test
	public void testMultiCycleWithOverridableBinary() throws IOException {
		this.clearModuleTarget();
		try {
			this.getWinterCompiler().compile(MODULEI);
			
			WinterTestCompiler extraCompiler = this.getWinterCompiler().withModulePaths(List.of(new File(this.getWinterCompiler().getModuleOutputPath(), MODULEJ)));
			extraCompiler.compile(MODULEJ);
			Assertions.fail("Should throw a WinterCompilationException");
		}
		catch(WinterCompilationException e) {
			Assertions.assertEquals(1, e.getDiagnostics().size());
			
			String cycleMessage1 = "Bean io.winterframework.test.multicycle.moduleJ:beanIOverride forms a cycle in module io.winterframework.test.multicycle.moduleJ\n" + 
					"  ┌────────────────────────────────┐\n" + 
					"  │                                │\n" + 
					"  │    io.winterframework.test.multicycle.moduleJ:beanIOverride\n" + 
					"  │                                │\n" + 
					"  │                                │ io.winterframework.test.multicycle.moduleJ:beanIOverride:runnable\n" + 
					"  │                                │\n" + 
					"  ▲                                ▼\n" + 
					"  │     io.winterframework.test.multicycle.moduleI:someRunnable\n" + 
					"  │                                ┊\n" + 
					"  │                               (┄) io.winterframework.test.multicycle.moduleI:beanI\n" + 
					"  │                                ┊\n" + 
					"  └────────────────────────────────┘ ";
			
			Assertions.assertEquals(cycleMessage1, e.getDiagnostics().get(0).getMessage(Locale.getDefault()));
		}
	}
}
