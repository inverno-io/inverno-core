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
 * 
 * @author jkuhn
 *
 */
public class TestConflict extends AbstractWinterTest {

	private static final String MODULE = "io.winterframework.test.conflict";
	
	@Test
	public void testConflict() throws IOException {
		try {
			this.getWinterCompiler().compile(MODULE);
			Assertions.fail("Should throw a WinterCompilationException");
		}
		catch(WinterCompilationException e) {
			Assertions.assertEquals(1, e.getDiagnostics().size());
			
			String conflictMessage = "Multiple beans matching socket io.winterframework.test.conflict:beanA:svc were found\n" + 
					"  - io.winterframework.test.conflict:serviceB of type io.winterframework.test.conflict.ServiceB\n" + 
					"  - io.winterframework.test.conflict:serviceA of type io.winterframework.test.conflict.ServiceA\n" + 
					"  \n" + 
					"  Consider specifying an explicit wiring in module io.winterframework.test.conflict (eg. @io.winterframework.core.annotation.Wire(beans=\"io.winterframework.test.conflict:serviceB\", into=\"io.winterframework.test.conflict:beanA:svc\") )\n" + 
					"   ";

			Assertions.assertEquals(conflictMessage, e.getDiagnostics().get(0).getMessage(Locale.getDefault()));
		}
	}
}
