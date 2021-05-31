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
import java.util.Locale;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import io.inverno.test.InvernoCompilationException;

/**
 * @author <a href="mailto:jeremy.kuhn@inverno.io">Jeremy Kuhn</a>
 *
 */
public class TestCycle extends AbstractCoreInvernoTest {

	private static final String MODULEA = "io.inverno.core.test.cycle.moduleA";
	private static final String MODULEB = "io.inverno.core.test.cycle.moduleB";
	private static final String MODULEC = "io.inverno.core.test.cycle.moduleC";

	@Test
	public void testCycle() throws IOException {
		try {
			this.getInvernoCompiler().compile(MODULEA);
			Assertions.fail("Should throw an InvernoCompilationException");
		}
		catch(InvernoCompilationException e) {
			Assertions.assertEquals(3, e.getDiagnostics().size());

			String cycleMessage1 = "Bean io.inverno.core.test.cycle.moduleA:beanA forms a cycle in module io.inverno.core.test.cycle.moduleA\n" + 
					"  ┌────────────────────────┐\n" + 
					"  │                        │\n" + 
					"  │    io.inverno.core.test.cycle.moduleA:beanA\n" + 
					"  │                        │\n" + 
					"  │                        │ io.inverno.core.test.cycle.moduleA:beanA:beanB\n" + 
					"  │                        │\n" + 
					"  │                        ▼\n" + 
					"  │    io.inverno.core.test.cycle.moduleA:beanB\n" + 
					"  ▲                        │\n" + 
					"  │                        │ io.inverno.core.test.cycle.moduleA:beanB:beanC\n" + 
					"  │                        │\n" + 
					"  │                        ▼\n" + 
					"  │    io.inverno.core.test.cycle.moduleA:beanC\n" + 
					"  │                        │\n" + 
					"  │                        │ io.inverno.core.test.cycle.moduleA:beanC:beanA\n" + 
					"  │                        │\n" + 
					"  └────────────────────────┘ ";
			
			Assertions.assertEquals(cycleMessage1, e.getDiagnostics().get(0).getMessage(Locale.getDefault()));
			
			String cycleMessage2 = "Bean io.inverno.core.test.cycle.moduleA:beanB forms a cycle in module io.inverno.core.test.cycle.moduleA\n" + 
					"  ┌────────────────────────┐\n" + 
					"  │                        │\n" + 
					"  │    io.inverno.core.test.cycle.moduleA:beanA\n" + 
					"  │                        │\n" + 
					"  │                        │ io.inverno.core.test.cycle.moduleA:beanA:beanB\n" + 
					"  │                        │\n" + 
					"  │                        ▼\n" + 
					"  │    io.inverno.core.test.cycle.moduleA:beanB\n" + 
					"  ▲                        │\n" + 
					"  │                        │ io.inverno.core.test.cycle.moduleA:beanB:beanC\n" + 
					"  │                        │\n" + 
					"  │                        ▼\n" + 
					"  │    io.inverno.core.test.cycle.moduleA:beanC\n" + 
					"  │                        │\n" + 
					"  │                        │ io.inverno.core.test.cycle.moduleA:beanC:beanA\n" + 
					"  │                        │\n" + 
					"  └────────────────────────┘ ";
			
			Assertions.assertEquals(cycleMessage2, e.getDiagnostics().get(1).getMessage(Locale.getDefault()));
			
			String cycleMessage3 = "Bean io.inverno.core.test.cycle.moduleA:beanC forms a cycle in module io.inverno.core.test.cycle.moduleA\n" + 
					"  ┌────────────────────────┐\n" + 
					"  │                        │\n" + 
					"  │    io.inverno.core.test.cycle.moduleA:beanA\n" + 
					"  │                        │\n" + 
					"  │                        │ io.inverno.core.test.cycle.moduleA:beanA:beanB\n" + 
					"  │                        │\n" + 
					"  │                        ▼\n" + 
					"  │    io.inverno.core.test.cycle.moduleA:beanB\n" + 
					"  ▲                        │\n" + 
					"  │                        │ io.inverno.core.test.cycle.moduleA:beanB:beanC\n" + 
					"  │                        │\n" + 
					"  │                        ▼\n" + 
					"  │    io.inverno.core.test.cycle.moduleA:beanC\n" + 
					"  │                        │\n" + 
					"  │                        │ io.inverno.core.test.cycle.moduleA:beanC:beanA\n" + 
					"  │                        │\n" + 
					"  └────────────────────────┘ "; 
			
			Assertions.assertEquals(cycleMessage3, e.getDiagnostics().get(2).getMessage(Locale.getDefault()));
		}
	}
	
	@Test
	public void testCycleWithNested() throws IOException {
		try {
			this.getInvernoCompiler().compile(MODULEB);
			Assertions.fail("Should throw an InvernoCompilationException");
		}
		catch(InvernoCompilationException e) {
			Assertions.assertEquals(4, e.getDiagnostics().size());
			
			String cycleMessage1 = "Bean io.inverno.core.test.cycle.moduleB:beanA forms a cycle in module io.inverno.core.test.cycle.moduleB\n" + 
					"  ┌──────────────────────────────┐\n" + 
					"  │                              │\n" + 
					"  │          io.inverno.core.test.cycle.moduleB:beanA\n" + 
					"  │                              │\n" + 
					"  │                              │ io.inverno.core.test.cycle.moduleB:beanA:beanB\n" + 
					"  │                              │\n" + 
					"  │                              ▼\n" + 
					"  │          io.inverno.core.test.cycle.moduleB:beanB\n" + 
					"  │                              │\n" + 
					"  │                              │ io.inverno.core.test.cycle.moduleB:beanB:beanC\n" + 
					"  │                              │\n" + 
					"  ▲                              ▼\n" + 
					"  │          io.inverno.core.test.cycle.moduleB:beanC\n" + 
					"  │                              │\n" + 
					"  │                              │ io.inverno.core.test.cycle.moduleB:beanC:runnable\n" + 
					"  │                              │\n" + 
					"  │                              ▼\n" + 
					"  │    io.inverno.core.test.cycle.moduleB:beanA.someRunnable\n" + 
					"  │                              │\n" + 
					"  │                              │ (nested)\n" + 
					"  │                              │\n" + 
					"  └──────────────────────────────┘ ";
			
			Assertions.assertEquals(cycleMessage1, e.getDiagnostics().get(0).getMessage(Locale.getDefault()));
			
			String cycleMessage2 = "Bean io.inverno.core.test.cycle.moduleB:beanB forms a cycle in module io.inverno.core.test.cycle.moduleB\n" + 
					"  ┌──────────────────────────────┐\n" + 
					"  │                              │\n" + 
					"  │          io.inverno.core.test.cycle.moduleB:beanA\n" + 
					"  │                              │\n" + 
					"  │                              │ io.inverno.core.test.cycle.moduleB:beanA:beanB\n" + 
					"  │                              │\n" + 
					"  │                              ▼\n" + 
					"  │          io.inverno.core.test.cycle.moduleB:beanB\n" + 
					"  │                              │\n" + 
					"  │                              │ io.inverno.core.test.cycle.moduleB:beanB:beanC\n" + 
					"  │                              │\n" + 
					"  ▲                              ▼\n" + 
					"  │          io.inverno.core.test.cycle.moduleB:beanC\n" + 
					"  │                              │\n" + 
					"  │                              │ io.inverno.core.test.cycle.moduleB:beanC:runnable\n" + 
					"  │                              │\n" + 
					"  │                              ▼\n" + 
					"  │    io.inverno.core.test.cycle.moduleB:beanA.someRunnable\n" + 
					"  │                              │\n" + 
					"  │                              │ (nested)\n" + 
					"  │                              │\n" + 
					"  └──────────────────────────────┘ ";
			
			Assertions.assertEquals(cycleMessage2, e.getDiagnostics().get(1).getMessage(Locale.getDefault()));
			
			String cycleMessage3 = "Bean io.inverno.core.test.cycle.moduleB:beanC forms a cycle in module io.inverno.core.test.cycle.moduleB\n" + 
					"  ┌──────────────────────────────┐\n" + 
					"  │                              │\n" + 
					"  │          io.inverno.core.test.cycle.moduleB:beanA\n" + 
					"  │                              │\n" + 
					"  │                              │ io.inverno.core.test.cycle.moduleB:beanA:beanB\n" + 
					"  │                              │\n" + 
					"  │                              ▼\n" + 
					"  │          io.inverno.core.test.cycle.moduleB:beanB\n" + 
					"  │                              │\n" + 
					"  │                              │ io.inverno.core.test.cycle.moduleB:beanB:beanC\n" + 
					"  │                              │\n" + 
					"  ▲                              ▼\n" + 
					"  │          io.inverno.core.test.cycle.moduleB:beanC\n" + 
					"  │                              │\n" + 
					"  │                              │ io.inverno.core.test.cycle.moduleB:beanC:runnable\n" + 
					"  │                              │\n" + 
					"  │                              ▼\n" + 
					"  │    io.inverno.core.test.cycle.moduleB:beanA.someRunnable\n" + 
					"  │                              │\n" + 
					"  │                              │ (nested)\n" + 
					"  │                              │\n" + 
					"  └──────────────────────────────┘ "; 
			
			Assertions.assertEquals(cycleMessage3, e.getDiagnostics().get(2).getMessage(Locale.getDefault()));
			
			String cycleMessage4 = "Bean io.inverno.core.test.cycle.moduleB:beanA.someRunnable forms a cycle in module io.inverno.core.test.cycle.moduleB\n" + 
					"  ┌──────────────────────────────┐\n" + 
					"  │                              │\n" + 
					"  │          io.inverno.core.test.cycle.moduleB:beanA\n" + 
					"  │                              │\n" + 
					"  │                              │ io.inverno.core.test.cycle.moduleB:beanA:beanB\n" + 
					"  │                              │\n" + 
					"  │                              ▼\n" + 
					"  │          io.inverno.core.test.cycle.moduleB:beanB\n" + 
					"  │                              │\n" + 
					"  │                              │ io.inverno.core.test.cycle.moduleB:beanB:beanC\n" + 
					"  │                              │\n" + 
					"  ▲                              ▼\n" + 
					"  │          io.inverno.core.test.cycle.moduleB:beanC\n" + 
					"  │                              │\n" + 
					"  │                              │ io.inverno.core.test.cycle.moduleB:beanC:runnable\n" + 
					"  │                              │\n" + 
					"  │                              ▼\n" + 
					"  │    io.inverno.core.test.cycle.moduleB:beanA.someRunnable\n" + 
					"  │                              │\n" + 
					"  │                              │ (nested)\n" + 
					"  │                              │\n" + 
					"  └──────────────────────────────┘ "; 
			
			Assertions.assertEquals(cycleMessage4, e.getDiagnostics().get(3).getMessage(Locale.getDefault()));
		}
	}
	
	@Test
	public void testCycleWithMultiNested() throws IOException {
		try {
			this.getInvernoCompiler().compile(MODULEC);
			Assertions.fail("Should throw an InvernoCompilationException");
		}
		catch(InvernoCompilationException e) {
			Assertions.assertEquals(3, e.getDiagnostics().size());
			
			String cycleMessage1 = "Bean io.inverno.core.test.cycle.moduleC:beanA forms a cycle in module io.inverno.core.test.cycle.moduleC\n" + 
					"  ┌────────────────────────────────────┐\n" + 
					"  │                                    │\n" + 
					"  │                io.inverno.core.test.cycle.moduleC:beanA\n" + 
					"  │                                    │\n" + 
					"  │                                    │ io.inverno.core.test.cycle.moduleC:beanA:beanB\n" + 
					"  │                                    │\n" + 
					"  │                                    ▼\n" + 
					"  │                io.inverno.core.test.cycle.moduleC:beanB\n" + 
					"  │                                    │\n" + 
					"  │                                    │ io.inverno.core.test.cycle.moduleC:beanB:runnable\n" + 
					"  │                                    │\n" + 
					"  ▲                                    ▼\n" + 
					"  │    io.inverno.core.test.cycle.moduleC:beanA.someNested.someRunnable\n" + 
					"  │                                    │\n" + 
					"  │                                    │ (nested)\n" + 
					"  │                                    │\n" + 
					"  │                                    ▼\n" + 
					"  │           io.inverno.core.test.cycle.moduleC:beanA.someNested\n" + 
					"  │                                    │\n" + 
					"  │                                    │ (nested)\n" + 
					"  │                                    │\n" + 
					"  └────────────────────────────────────┘ ";
			
			Assertions.assertEquals(cycleMessage1, e.getDiagnostics().get(0).getMessage(Locale.getDefault()));
			
			String cycleMessage2 = "Bean io.inverno.core.test.cycle.moduleC:beanB forms a cycle in module io.inverno.core.test.cycle.moduleC\n" + 
					"  ┌────────────────────────────────────┐\n" + 
					"  │                                    │\n" + 
					"  │                io.inverno.core.test.cycle.moduleC:beanA\n" + 
					"  │                                    │\n" + 
					"  │                                    │ io.inverno.core.test.cycle.moduleC:beanA:beanB\n" + 
					"  │                                    │\n" + 
					"  │                                    ▼\n" + 
					"  │                io.inverno.core.test.cycle.moduleC:beanB\n" + 
					"  │                                    │\n" + 
					"  │                                    │ io.inverno.core.test.cycle.moduleC:beanB:runnable\n" + 
					"  │                                    │\n" + 
					"  ▲                                    ▼\n" + 
					"  │    io.inverno.core.test.cycle.moduleC:beanA.someNested.someRunnable\n" + 
					"  │                                    │\n" + 
					"  │                                    │ (nested)\n" + 
					"  │                                    │\n" + 
					"  │                                    ▼\n" + 
					"  │           io.inverno.core.test.cycle.moduleC:beanA.someNested\n" + 
					"  │                                    │\n" + 
					"  │                                    │ (nested)\n" + 
					"  │                                    │\n" + 
					"  └────────────────────────────────────┘ ";
			
			Assertions.assertEquals(cycleMessage2, e.getDiagnostics().get(1).getMessage(Locale.getDefault()));
			
			String cycleMessage3 = "Bean io.inverno.core.test.cycle.moduleC:beanA.someNested forms a cycle in module io.inverno.core.test.cycle.moduleC\n" + 
					"  ┌────────────────────────────────────┐\n" + 
					"  │                                    │\n" + 
					"  │                io.inverno.core.test.cycle.moduleC:beanA\n" + 
					"  │                                    │\n" + 
					"  │                                    │ io.inverno.core.test.cycle.moduleC:beanA:beanB\n" + 
					"  │                                    │\n" + 
					"  │                                    ▼\n" + 
					"  │                io.inverno.core.test.cycle.moduleC:beanB\n" + 
					"  │                                    │\n" + 
					"  │                                    │ io.inverno.core.test.cycle.moduleC:beanB:runnable\n" + 
					"  │                                    │\n" + 
					"  ▲                                    ▼\n" + 
					"  │    io.inverno.core.test.cycle.moduleC:beanA.someNested.someRunnable\n" + 
					"  │                                    │\n" + 
					"  │                                    │ (nested)\n" + 
					"  │                                    │\n" + 
					"  │                                    ▼\n" + 
					"  │           io.inverno.core.test.cycle.moduleC:beanA.someNested\n" + 
					"  │                                    │\n" + 
					"  │                                    │ (nested)\n" + 
					"  │                                    │\n" + 
					"  └────────────────────────────────────┘ "; 
			
			Assertions.assertEquals(cycleMessage3, e.getDiagnostics().get(2).getMessage(Locale.getDefault()));
		}
	}
}
