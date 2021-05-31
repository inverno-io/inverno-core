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
public class TestMissing extends AbstractCoreInvernoTest {

	private static final String MODULE = "io.inverno.core.test.missing";

	@Test
	public void testMissing() throws IOException {
		try {
			this.getInvernoCompiler().compile(MODULE);
			Assertions.fail("Should throw an InvernoCompilationException");
		}
		catch(InvernoCompilationException e) {
			Assertions.assertEquals(2, e.getDiagnostics().size());
			
			String missingSingleMessage = "No bean was found matching required socket io.inverno.core.test.missing:beanA:dataSource of type javax.sql.DataSource, consider defining a bean or a socket bean matching the socket in module io.inverno.core.test.missing";
			String missingMultiMessage = "No bean was found matching required socket io.inverno.core.test.missing:beanB:dataSources of type javax.sql.DataSource, consider defining a bean or socket bean matching the socket in module io.inverno.core.test.missing";
			
			Assertions.assertTrue(e.getDiagnostics().stream().map(d -> d.getMessage(Locale.getDefault())).collect(Collectors.toList()).containsAll(List.of(missingSingleMessage, missingMultiMessage)));
		}
	}
}
