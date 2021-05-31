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

/**
 * @author <a href="mailto:jeremy.kuhn@inverno.io">Jeremy Kuhn</a>
 *
 */
public class TestMultiCycle extends AbstractCoreInvernoTest {

	private static final String MODULEAPI = "io.inverno.core.test.multicycle.moduleAPI";
	
	private static final String MODULEA = "io.inverno.core.test.multicycle.moduleA";
	private static final String MODULEB = "io.inverno.core.test.multicycle.moduleB";
	
	private static final String MODULEC = "io.inverno.core.test.multicycle.moduleC";
	private static final String MODULED = "io.inverno.core.test.multicycle.moduleD";
	private static final String MODULEE = "io.inverno.core.test.multicycle.moduleE";
	private static final String MODULEF = "io.inverno.core.test.multicycle.moduleF";
	
	private static final String MODULEG = "io.inverno.core.test.multicycle.moduleG";
	private static final String MODULEH = "io.inverno.core.test.multicycle.moduleH";

	private static final String MODULEI = "io.inverno.core.test.multicycle.moduleI";
	private static final String MODULEJ = "io.inverno.core.test.multicycle.moduleJ";
	
	@Test
	public void testMultiCycleSimple() throws IOException {
		this.clearModuleTarget();
		try {
			this.getInvernoCompiler().compile(MODULEA, MODULEB);
			Assertions.fail("Should throw an InvernoCompilationException");
		}
		catch(InvernoCompilationException e) {
			Assertions.assertEquals(1, e.getDiagnostics().size());
			
			String cycleMessage = "Bean io.inverno.core.test.multicycle.moduleA:beanA forms a cycle in module io.inverno.core.test.multicycle.moduleA\n" + 
				"  ┌──────────────────────────┐\n" + 
				"  │                          │\n" + 
				"  │    io.inverno.core.test.multicycle.moduleA:beanA\n" + 
				"  │                          │\n" + 
				"  │                          │ io.inverno.core.test.multicycle.moduleA:beanA:callable\n" + 
				"  │                          │\n" + 
				"  │                          ▼\n" + 
				"  │    io.inverno.core.test.multicycle.moduleB:beanA\n" + 
				"  ▲                          │\n" + 
				"  │                          │ io.inverno.core.test.multicycle.moduleB:beanA:runnable\n" + 
				"  │                          │\n" + 
				"  │                          ▼\n" + 
				"  │                          ┊\n" + 
				"  │                          ┊ io.inverno.core.test.multicycle.moduleB:runnableSocket\n" + 
				"  │                          ┊\n" + 
				"  └──────────────────────────┘ "; 
			
			Assertions.assertEquals(cycleMessage, e.getDiagnostics().get(0).getMessage(Locale.getDefault()));
		}
	}
	
	@Test
	public void testMultiCycleComplex() throws IOException, InvernoCompilationException {
		this.clearModuleTarget();
		try {
			this.getInvernoCompiler().compile(MODULEAPI, MODULEF, MODULED, MODULEE, MODULEC);
			Assertions.fail("Should throw an InvernoCompilationException");
		}
		catch(InvernoCompilationException e) {
			Assertions.assertEquals(1, e.getDiagnostics().size());
			
			String cycleMessage = "Bean io.inverno.core.test.multicycle.moduleC:serviceCImpl forms a cycle in module io.inverno.core.test.multicycle.moduleC\n" + 
				"  ┌──────────────────────────────┐\n" + 
				"  │                              │\n" + 
				"  │    io.inverno.core.test.multicycle.moduleC:serviceCImpl\n" + 
				"  │                              │\n" + 
				"  │                              │ io.inverno.core.test.multicycle.moduleC:serviceCImpl:serviceD\n" + 
				"  │                              │\n" + 
				"  │                              ▼\n" + 
				"  │    io.inverno.core.test.multicycle.moduleD:serviceDImpl\n" + 
				"  │                              │\n" + 
				"  │                              │ io.inverno.core.test.multicycle.moduleD:serviceDImpl:beanA\n" + 
				"  │                              │\n" + 
				"  │                              ▼\n" + 
				"  │        io.inverno.core.test.multicycle.moduleD:beanA\n" + 
				"  │                              │\n" + 
				"  │                              │ io.inverno.core.test.multicycle.moduleD:beanA:serviceE\n" + 
				"  │                              │\n" + 
				"  │                              ▼\n" + 
				"  │                              ┊\n" + 
				"  │                              ┊ io.inverno.core.test.multicycle.moduleD:serviceESocket\n" + 
				"  ▲                              ┊\n" + 
				"  │                              ▼\n" + 
				"  │    io.inverno.core.test.multicycle.moduleE:serviceEImpl\n" + 
				"  │                              │\n" + 
				"  │                              │ io.inverno.core.test.multicycle.moduleE:serviceEImpl:serviceF\n" + 
				"  │                              │\n" + 
				"  │                              ▼\n" + 
				"  │    io.inverno.core.test.multicycle.moduleF:serviceFImpl\n" + 
				"  │                              │\n" + 
				"  │                              │ io.inverno.core.test.multicycle.moduleF:serviceFImpl:serviceC\n" + 
				"  │                              │\n" + 
				"  │                              ▼\n" + 
				"  │                              ┊\n" + 
				"  │                              ┊ io.inverno.core.test.multicycle.moduleF:serviceCSocket\n" + 
				"  │                              ┊\n" + 
				"  │                              ▼\n" + 
				"  │                              ┊\n" + 
				"  │                              ┊ io.inverno.core.test.multicycle.moduleE:serviceCSocket\n" + 
				"  │                              ┊\n" + 
				"  └──────────────────────────────┘ "; 
			
			Assertions.assertEquals(cycleMessage, e.getDiagnostics().get(0).getMessage(Locale.getDefault()));
		}
	}
	
	@Test
	public void testMultiCycleSimpleBinary() throws IOException, InvernoCompilationException {
		this.clearModuleTarget();
		try {
			this.getInvernoCompiler().compile(MODULEB);
		
			InvernoTestCompiler extraCompiler = this.getInvernoCompiler().withModulePaths(List.of(new File(this.getInvernoCompiler().getModuleOutputPath(), MODULEA)));
			
			extraCompiler.compile(MODULEA);
			Assertions.fail("Should throw an InvernoCompilationException");
		}
		catch(InvernoCompilationException e) {
			Assertions.assertEquals(1, e.getDiagnostics().size());
			
			String cycleMessage = "Bean io.inverno.core.test.multicycle.moduleA:beanA forms a cycle in module io.inverno.core.test.multicycle.moduleA\n" + 
				"  ┌──────────────────────────┐\n" + 
				"  │                          │\n" + 
				"  │    io.inverno.core.test.multicycle.moduleA:beanA\n" + 
				"  │                          │\n" + 
				"  │                          │ io.inverno.core.test.multicycle.moduleA:beanA:callable\n" + 
				"  │                          │\n" + 
				"  ▲                          ▼\n" + 
				"  │    io.inverno.core.test.multicycle.moduleB:beanA\n" + 
				"  │                          ┊\n" + 
				"  │                         (┄) io.inverno.core.test.multicycle.moduleB:runnableSocket\n" + 
				"  │                          ┊\n" + 
				"  └──────────────────────────┘ "; 
			
			Assertions.assertEquals(cycleMessage, e.getDiagnostics().get(0).getMessage(Locale.getDefault()));
		}		
	}
	
	@Test
	public void testMultiCycleWithNested() throws IOException {
		this.clearModuleTarget();
		try {
			this.getInvernoCompiler().compile(MODULEG, MODULEH);
			Assertions.fail("Should throw an InvernoCompilationException");
		}
		catch(InvernoCompilationException e) {
			Assertions.assertEquals(2, e.getDiagnostics().size());
			
			String cycleMessage1 = "Bean io.inverno.core.test.multicycle.moduleH:beanH forms a cycle in module io.inverno.core.test.multicycle.moduleH\n" + 
					"  ┌─────────────────────────────────┐\n" + 
					"  │                                 │\n" + 
					"  │           io.inverno.core.test.multicycle.moduleH:beanH\n" + 
					"  │                                 │\n" + 
					"  │                                 │ io.inverno.core.test.multicycle.moduleH:beanH:beanG\n" + 
					"  │                                 │\n" + 
					"  │                                 ▼\n" + 
					"  │           io.inverno.core.test.multicycle.moduleG:beanG\n" + 
					"  │                                 │\n" + 
					"  │                                 │ io.inverno.core.test.multicycle.moduleG:beanG:runnable\n" + 
					"  ▲                                 │\n" + 
					"  │                                 ▼\n" + 
					"  │                                 ┊\n" + 
					"  │                                 ┊ io.inverno.core.test.multicycle.moduleG:runnableSocket\n" + 
					"  │                                 ┊\n" + 
					"  │                                 ▼\n" + 
					"  │    io.inverno.core.test.multicycle.moduleH:beanH.someRunnable\n" + 
					"  │                                 │\n" + 
					"  │                                 │ (nested)\n" + 
					"  │                                 │\n" + 
					"  └─────────────────────────────────┘ ";
			
			Assertions.assertEquals(cycleMessage1, e.getDiagnostics().get(0).getMessage(Locale.getDefault()));
			
			String cycleMessage2 = "Bean io.inverno.core.test.multicycle.moduleH:beanH.someRunnable forms a cycle in module io.inverno.core.test.multicycle.moduleH\n" + 
					"  ┌─────────────────────────────────┐\n" + 
					"  │                                 │\n" + 
					"  │           io.inverno.core.test.multicycle.moduleH:beanH\n" + 
					"  │                                 │\n" + 
					"  │                                 │ io.inverno.core.test.multicycle.moduleH:beanH:beanG\n" + 
					"  │                                 │\n" + 
					"  │                                 ▼\n" + 
					"  │           io.inverno.core.test.multicycle.moduleG:beanG\n" + 
					"  │                                 │\n" + 
					"  │                                 │ io.inverno.core.test.multicycle.moduleG:beanG:runnable\n" + 
					"  ▲                                 │\n" + 
					"  │                                 ▼\n" + 
					"  │                                 ┊\n" + 
					"  │                                 ┊ io.inverno.core.test.multicycle.moduleG:runnableSocket\n" + 
					"  │                                 ┊\n" + 
					"  │                                 ▼\n" + 
					"  │    io.inverno.core.test.multicycle.moduleH:beanH.someRunnable\n" + 
					"  │                                 │\n" + 
					"  │                                 │ (nested)\n" + 
					"  │                                 │\n" + 
					"  └─────────────────────────────────┘ ";
			
			Assertions.assertEquals(cycleMessage2, e.getDiagnostics().get(1).getMessage(Locale.getDefault()));
		}
	}
	
	@Test
	public void testMultiCycleWithNestedBinary() throws IOException {
		this.clearModuleTarget();
		try {
			this.getInvernoCompiler().compile(MODULEG);
		
			InvernoTestCompiler extraCompiler = this.getInvernoCompiler().withModulePaths(List.of(new File(this.getInvernoCompiler().getModuleOutputPath(), MODULEH)));
			extraCompiler.compile(MODULEH);
			Assertions.fail("Should throw an InvernoCompilationException");
		}
		catch(InvernoCompilationException e) {
			Assertions.assertEquals(2, e.getDiagnostics().size());
			
			String cycleMessage1 = "Bean io.inverno.core.test.multicycle.moduleH:beanH forms a cycle in module io.inverno.core.test.multicycle.moduleH\n" + 
					"  ┌─────────────────────────────────┐\n" + 
					"  │                                 │\n" + 
					"  │           io.inverno.core.test.multicycle.moduleH:beanH\n" + 
					"  │                                 │\n" + 
					"  │                                 │ io.inverno.core.test.multicycle.moduleH:beanH:beanG\n" + 
					"  │                                 │\n" + 
					"  │                                 ▼\n" + 
					"  │           io.inverno.core.test.multicycle.moduleG:beanG\n" + 
					"  ▲                                 ┊\n" + 
					"  │                                (┄) io.inverno.core.test.multicycle.moduleG:runnableSocket\n" + 
					"  │                                 ┊\n" + 
					"  │                                 ▼\n" + 
					"  │    io.inverno.core.test.multicycle.moduleH:beanH.someRunnable\n" + 
					"  │                                 │\n" + 
					"  │                                 │ (nested)\n" + 
					"  │                                 │\n" + 
					"  └─────────────────────────────────┘ ";
			
			Assertions.assertEquals(cycleMessage1, e.getDiagnostics().get(0).getMessage(Locale.getDefault()));
			
			String cycleMessage2 = "Bean io.inverno.core.test.multicycle.moduleH:beanH.someRunnable forms a cycle in module io.inverno.core.test.multicycle.moduleH\n" + 
					"  ┌─────────────────────────────────┐\n" + 
					"  │                                 │\n" + 
					"  │           io.inverno.core.test.multicycle.moduleH:beanH\n" + 
					"  │                                 │\n" + 
					"  │                                 │ io.inverno.core.test.multicycle.moduleH:beanH:beanG\n" + 
					"  │                                 │\n" + 
					"  │                                 ▼\n" + 
					"  │           io.inverno.core.test.multicycle.moduleG:beanG\n" + 
					"  ▲                                 ┊\n" + 
					"  │                                (┄) io.inverno.core.test.multicycle.moduleG:runnableSocket\n" + 
					"  │                                 ┊\n" + 
					"  │                                 ▼\n" + 
					"  │    io.inverno.core.test.multicycle.moduleH:beanH.someRunnable\n" + 
					"  │                                 │\n" + 
					"  │                                 │ (nested)\n" + 
					"  │                                 │\n" + 
					"  └─────────────────────────────────┘ ";
			
			Assertions.assertEquals(cycleMessage2, e.getDiagnostics().get(1).getMessage(Locale.getDefault()));
		}
	}
	
	@Test
	public void testMultiCycleWithOverridable() throws IOException {
		this.clearModuleTarget();
		try {
			this.getInvernoCompiler().compile(MODULEI, MODULEJ);
			Assertions.fail("Should throw an InvernoCompilationException");
		}
		catch(InvernoCompilationException e) {
			Assertions.assertEquals(1, e.getDiagnostics().size());
			
			String cycleMessage1 = "Bean io.inverno.core.test.multicycle.moduleJ:beanIOverride forms a cycle in module io.inverno.core.test.multicycle.moduleJ\n" + 
					"  ┌──────────────────────────────┐\n" + 
					"  │                              │\n" + 
					"  │    io.inverno.core.test.multicycle.moduleJ:beanIOverride\n" + 
					"  │                              │\n" + 
					"  │                              │ io.inverno.core.test.multicycle.moduleJ:beanIOverride:runnable\n" + 
					"  │                              │\n" + 
					"  │                              ▼\n" + 
					"  │    io.inverno.core.test.multicycle.moduleI:someRunnable\n" + 
					"  ▲                              │\n" + 
					"  │                              │ io.inverno.core.test.multicycle.moduleI:someRunnable:beanI\n" + 
					"  │                              │\n" + 
					"  │                              ▼\n" + 
					"  │                              ┊\n" + 
					"  │                              ┊ io.inverno.core.test.multicycle.moduleI:beanI\n" + 
					"  │                              ┊\n" + 
					"  └──────────────────────────────┘ ";
			
			Assertions.assertEquals(cycleMessage1, e.getDiagnostics().get(0).getMessage(Locale.getDefault()));
		}
	}
	
	@Test
	public void testMultiCycleWithOverridableBinary() throws IOException {
		this.clearModuleTarget();
		try {
			this.getInvernoCompiler().compile(MODULEI);
			
			InvernoTestCompiler extraCompiler = this.getInvernoCompiler().withModulePaths(List.of(new File(this.getInvernoCompiler().getModuleOutputPath(), MODULEJ)));
			extraCompiler.compile(MODULEJ);
			Assertions.fail("Should throw an InvernoCompilationException");
		}
		catch(InvernoCompilationException e) {
			Assertions.assertEquals(1, e.getDiagnostics().size());
			
			String cycleMessage1 = "Bean io.inverno.core.test.multicycle.moduleJ:beanIOverride forms a cycle in module io.inverno.core.test.multicycle.moduleJ\n" + 
					"  ┌──────────────────────────────┐\n" + 
					"  │                              │\n" + 
					"  │    io.inverno.core.test.multicycle.moduleJ:beanIOverride\n" + 
					"  │                              │\n" + 
					"  │                              │ io.inverno.core.test.multicycle.moduleJ:beanIOverride:runnable\n" + 
					"  │                              │\n" + 
					"  ▲                              ▼\n" + 
					"  │    io.inverno.core.test.multicycle.moduleI:someRunnable\n" + 
					"  │                              ┊\n" + 
					"  │                             (┄) io.inverno.core.test.multicycle.moduleI:beanI\n" + 
					"  │                              ┊\n" + 
					"  └──────────────────────────────┘ ";
			
			Assertions.assertEquals(cycleMessage1, e.getDiagnostics().get(0).getMessage(Locale.getDefault()));
		}
	}
}
