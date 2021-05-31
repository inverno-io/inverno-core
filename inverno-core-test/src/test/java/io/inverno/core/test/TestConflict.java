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
 * 
 * @author <a href="mailto:jeremy.kuhn@inverno.io">Jeremy Kuhn</a>
 *
 */
public class TestConflict extends AbstractCoreInvernoTest {

	private static final String MODULE = "io.inverno.core.test.conflict";
	
	@Test
	public void testConflict() throws IOException {
		try {
			this.getInvernoCompiler().compile(MODULE);
			Assertions.fail("Should throw an InvernoCompilationException");
		}
		catch(InvernoCompilationException e) {
			Assertions.assertEquals(1, e.getDiagnostics().size());
			
			String conflictMessage = "Multiple beans matching socket io.inverno.core.test.conflict:beanA:svc were found\n" + 
					"  - io.inverno.core.test.conflict:serviceB of type io.inverno.core.test.conflict.ServiceB\n" + 
					"  - io.inverno.core.test.conflict:serviceA of type io.inverno.core.test.conflict.ServiceA\n" + 
					"  \n" + 
					"  Consider specifying an explicit wiring in module io.inverno.core.test.conflict (eg. @io.inverno.core.annotation.Wire(beans=\"io.inverno.core.test.conflict:serviceB\", into=\"io.inverno.core.test.conflict:beanA:svc\") )\n" + 
					"   ";

			Assertions.assertEquals(conflictMessage, e.getDiagnostics().get(0).getMessage(Locale.getDefault()));
		}
	}
}
