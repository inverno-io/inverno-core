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

import java.io.IOException;
import java.util.Locale;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import io.winterframework.core.test.AbstractWinterTest;
import io.winterframework.core.test.WinterCompilationException;

/**
 * @author jkuhn
 *
 */
public class TestCycle extends AbstractWinterTest {

	private static final String MODULE = "io.winterframework.test.cycle";

	@Test
	public void testCycle() throws IOException {
		try {
			this.getWinterCompiler().compile(MODULE);
			Assertions.fail("Should throw a WinterCompilationException");
		}
		catch(WinterCompilationException e) {
			Assertions.assertEquals(3, e.getDiagnostics().size());

			String cycleMessage1 = "Bean io.winterframework.test.cycle:beanA forms a cycle in module io.winterframework.test.cycle\n" + 
					"  ┌─────────────────────┐\n" + 
					"  │                     │\n" + 
					"  │    io.winterframework.test.cycle:beanA\n" + 
					"  │                     │\n" + 
					"  │                     │ io.winterframework.test.cycle:beanA:beanB\n" + 
					"  │                     │\n" + 
					"  │                     ▼\n" + 
					"  │    io.winterframework.test.cycle:beanB\n" + 
					"  ▲                     │\n" + 
					"  │                     │ io.winterframework.test.cycle:beanB:beanC\n" + 
					"  │                     │\n" + 
					"  │                     ▼\n" + 
					"  │    io.winterframework.test.cycle:beanC\n" + 
					"  │                     │\n" + 
					"  │                     │ io.winterframework.test.cycle:beanC:beanA\n" + 
					"  │                     │\n" + 
					"  └─────────────────────┘ "; 
			
			Assertions.assertEquals(cycleMessage1, e.getDiagnostics().get(0).getMessage(Locale.getDefault()));
			
			String cycleMessage2 = "Bean io.winterframework.test.cycle:beanB forms a cycle in module io.winterframework.test.cycle\n" + 
					"  ┌─────────────────────┐\n" + 
					"  │                     │\n" + 
					"  │    io.winterframework.test.cycle:beanA\n" + 
					"  │                     │\n" + 
					"  │                     │ io.winterframework.test.cycle:beanA:beanB\n" + 
					"  │                     │\n" + 
					"  │                     ▼\n" + 
					"  │    io.winterframework.test.cycle:beanB\n" + 
					"  ▲                     │\n" + 
					"  │                     │ io.winterframework.test.cycle:beanB:beanC\n" + 
					"  │                     │\n" + 
					"  │                     ▼\n" + 
					"  │    io.winterframework.test.cycle:beanC\n" + 
					"  │                     │\n" + 
					"  │                     │ io.winterframework.test.cycle:beanC:beanA\n" + 
					"  │                     │\n" + 
					"  └─────────────────────┘ ";
			
			Assertions.assertEquals(cycleMessage2, e.getDiagnostics().get(1).getMessage(Locale.getDefault()));
			
			String cycleMessage3 = "Bean io.winterframework.test.cycle:beanC forms a cycle in module io.winterframework.test.cycle\n" + 
					"  ┌─────────────────────┐\n" + 
					"  │                     │\n" + 
					"  │    io.winterframework.test.cycle:beanA\n" + 
					"  │                     │\n" + 
					"  │                     │ io.winterframework.test.cycle:beanA:beanB\n" + 
					"  │                     │\n" + 
					"  │                     ▼\n" + 
					"  │    io.winterframework.test.cycle:beanB\n" + 
					"  ▲                     │\n" + 
					"  │                     │ io.winterframework.test.cycle:beanB:beanC\n" + 
					"  │                     │\n" + 
					"  │                     ▼\n" + 
					"  │    io.winterframework.test.cycle:beanC\n" + 
					"  │                     │\n" + 
					"  │                     │ io.winterframework.test.cycle:beanC:beanA\n" + 
					"  │                     │\n" + 
					"  └─────────────────────┘ "; 
			
			Assertions.assertEquals(cycleMessage3, e.getDiagnostics().get(2).getMessage(Locale.getDefault()));
		}
	}
}
