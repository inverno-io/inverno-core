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
import java.util.Locale;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import io.inverno.test.InvernoCompilationException;

/**
 * 
 * @author <a href="mailto:jeremy.kuhn@inverno.io">Jeremy Kuhn</a>
 *
 */
public class TestCompilationWarning extends AbstractCoreInvernoTest {

	private static final String MODULEA = "io.inverno.core.test.warning.moduleA";
	
	@Test
	public void testInvalidOptionalSocket() throws IOException, InvernoCompilationException {
		this.getInvernoCompiler().compile(MODULEA);
		
		this.getInvernoCompiler().getDiagnostics();
		
		Assertions.assertEquals(1, this.getInvernoCompiler().getDiagnostics().size());
		
		String invalidOptionalSocket = "Invalid socket method which should be a single-argument setter method, socket will be ignored";
		
		Assertions.assertTrue(this.getInvernoCompiler().getDiagnostics().stream().map(d -> d.getMessage(Locale.getDefault())).collect(Collectors.toList()).containsAll(List.of(invalidOptionalSocket)));
	}
}
