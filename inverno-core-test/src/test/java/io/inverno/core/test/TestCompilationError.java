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
import java.util.Set;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import io.inverno.test.InvernoCompilationException;

/**
 * 
 * @author <a href="mailto:jeremy.kuhn@inverno.io">Jeremy Kuhn</a>
 *
 */
public class TestCompilationError extends AbstractCoreInvernoTest {

	private static final String MODULEA = "io.inverno.core.test.error.moduleA";
	private static final String MODULEB = "io.inverno.core.test.error.moduleB";
	private static final String MODULEC = "io.inverno.core.test.error.moduleC";
	private static final String MODULED = "io.inverno.core.test.error.moduleD";
	private static final String MODULEE = "io.inverno.core.test.error.moduleE";
	private static final String MODULEF = "io.inverno.core.test.error.moduleF";
	private static final String MODULEG = "io.inverno.core.test.error.moduleG";
	private static final String MODULEH = "io.inverno.core.test.error.moduleH";
	private static final String MODULEI = "io.inverno.core.test.error.moduleI";
	private static final String MODULEJ = "io.inverno.core.test.error.moduleJ";
	private static final String MODULEK = "io.inverno.core.test.error.moduleK";
	private static final String MODULEL = "io.inverno.core.test.error.moduleL";
	
	@Test
	public void testBeanConcreteClass() throws IOException {
		try {
			this.getInvernoCompiler().compile(MODULEA);
			Assertions.fail("Should throw an InvernoCompilationException");
		}
		catch(InvernoCompilationException e) {
			Assertions.assertEquals(2, e.getDiagnostics().size());
			
			String abstractBeanError = "A bean must be a concrete class";
			String socketBeanSupplierError = "A socket bean must extend java.util.function.Supplier";
			
			Assertions.assertTrue(e.getDiagnostics().stream().map(d -> d.getMessage(Locale.getDefault())).collect(Collectors.toList()).containsAll(List.of(abstractBeanError, socketBeanSupplierError)));
		}
	}
	
	@Test
	public void testInvalidBeanQualifiedName() throws IOException {
		try {
			this.getInvernoCompiler().compile(MODULEB);
			Assertions.fail("Should throw an InvernoCompilationException");
		}
		catch(InvernoCompilationException e) {
			Assertions.assertEquals(1, e.getDiagnostics().size());
			
			String invalidBeanQualifiedNameError = "Invalid bean qualified name: QName part must be a valid Java identifier: #Invalid bean name";
			
			Assertions.assertTrue(e.getDiagnostics().stream().map(d -> d.getMessage(Locale.getDefault())).collect(Collectors.toList()).containsAll(List.of(invalidBeanQualifiedNameError)));
		}
	}
	
	@Test
	public void testPrivateConstructor() throws IOException {
		try {
			this.getInvernoCompiler().compile(MODULEC);
			Assertions.fail("Should throw an InvernoCompilationException");
		}
		catch(InvernoCompilationException e) {
			Assertions.assertEquals(1, e.getDiagnostics().size());
			
			String privateConstructorError = "No public constructor defined in bean io.inverno.core.test.error.moduleC:beanA";
			
			Assertions.assertTrue(e.getDiagnostics().stream().map(d -> d.getMessage(Locale.getDefault())).collect(Collectors.toList()).containsAll(List.of(privateConstructorError)));
		}
	}
	
	@Test
	public void testMultipleConstructors() throws IOException {
		try {
			this.getInvernoCompiler().compile(MODULED);
			Assertions.fail("Should throw an InvernoCompilationException");
		}
		catch(InvernoCompilationException e) {
			Assertions.assertEquals(1, e.getDiagnostics().size());
			
			String multipleConstructorsError = "Multiple constructors are defined in module bean io.inverno.core.test.error.moduleD:beanA, consider specifying a BeanSocket on the one to select";
			
			Assertions.assertTrue(e.getDiagnostics().stream().map(d -> d.getMessage(Locale.getDefault())).collect(Collectors.toList()).containsAll(List.of(multipleConstructorsError)));
		}
	}
	
	@Test
	public void testMultipleEnabledSocketConstructors() throws IOException {
		try {
			this.getInvernoCompiler().compile(MODULEE);
			Assertions.fail("Should throw an InvernoCompilationException");
		}
		catch(InvernoCompilationException e) {
			Assertions.assertEquals(1, e.getDiagnostics().size());
			
			String multipleSocketConstructorsError = "Multiple constructors annotated with BeanSocket are enabled in module bean io.inverno.core.test.error.moduleE:beanA, consider keeping only one enabled constructor";
			
			Assertions.assertTrue(e.getDiagnostics().stream().map(d -> d.getMessage(Locale.getDefault())).collect(Collectors.toList()).containsAll(List.of(multipleSocketConstructorsError)));
		}
	}
	
	@Test
	public void testOptionalRequiredSocketNameConflict() throws IOException {
		try {
			this.getInvernoCompiler().compile(MODULEF);
			Assertions.fail("Should throw an InvernoCompilationException");
		}
		catch(InvernoCompilationException e) {
			Set<String> messages = e.getDiagnostics().stream().map(d -> d.getMessage(Locale.getDefault())).collect(Collectors.toSet());
			
			Assertions.assertEquals(2, messages.size());
			
			String requiredSocketNameConflict = "Required socket name is conflicting with an optional socket: runnable";
			String optionalSocketNameConflict = "Optional socket name is conflicting with a required socket: runnable";
			
			Assertions.assertTrue(messages.containsAll(Set.of(requiredSocketNameConflict, optionalSocketNameConflict)));
		}
	}
	
	@Test
	public void testModuleBeanNameConflict() throws IOException {
		try {
			this.getInvernoCompiler().compile(MODULEG);
			Assertions.fail("Should throw an InvernoCompilationException");
		}
		catch(InvernoCompilationException e) {
			Set<String> messages = e.getDiagnostics().stream().map(d -> d.getMessage(Locale.getDefault())).collect(Collectors.toSet());
			
			Assertions.assertEquals(3, messages.size());
			
			String conflict1 = "Multiple beans with name beanD exist in module io.inverno.core.test.error.moduleG";
			String conflict2 = "Multiple beans with name beanA exist in module io.inverno.core.test.error.moduleG";
			String conflict3 = "Multiple beans with name beanC exist in module io.inverno.core.test.error.moduleG";

			Assertions.assertTrue(messages.containsAll(Set.of(conflict1, conflict2, conflict3)));
		}
	}

	@Test
	public void testModuleBeanModuleConflict() throws IOException {
		try {
			this.getInvernoCompiler().compile(MODULEH, MODULEI);
			Assertions.fail("Should throw an InvernoCompilationException");
		}
		catch(InvernoCompilationException e) {
			Assertions.assertEquals(1, e.getDiagnostics().size());
			
			String beanModuleNameConflict = "Bean is conflicting with module: io.inverno.core.test.error.moduleH";
			
			Assertions.assertTrue(e.getDiagnostics().stream().map(d -> d.getMessage(Locale.getDefault())).collect(Collectors.toList()).containsAll(List.of(beanModuleNameConflict)));
		}
	}
	
	@Test
	public void testOptionalOptionalSocketNameConflict() throws IOException {
		try {
			this.getInvernoCompiler().compile(MODULEJ);
			Assertions.fail("Should throw an InvernoCompilationException");
		}
		catch(InvernoCompilationException e) {
			Assertions.assertEquals(2, e.getDiagnostics().size());
			
			String optionaSocketNameConflict1 = "Optional socket name is conflicting with another optional socket: foo";
			String optionaSocketNameConflict2 = "Optional socket name is conflicting with another optional socket: foo";
			
			Assertions.assertTrue(e.getDiagnostics().stream().map(d -> d.getMessage(Locale.getDefault())).collect(Collectors.toList()).containsAll(List.of(optionaSocketNameConflict1, optionaSocketNameConflict2)));
		}
	}
	
	@Test
	public void testInvalidLazySocket() throws IOException {
		try {
			this.getInvernoCompiler().compile(MODULEK);
			Assertions.fail("Should throw an InvernoCompilationException");
		}
		catch(InvernoCompilationException e) {
			Assertions.assertEquals(1, e.getDiagnostics().size());
			
			String invalidLazySocket = "Invalid lazy socket which should be of type java.util.function.Supplier";
			
			Assertions.assertTrue(e.getDiagnostics().stream().map(d -> d.getMessage(Locale.getDefault())).collect(Collectors.toList()).containsAll(List.of(invalidLazySocket)));
		}
	}
	
	@Test
	public void testNoEnabledSocketConstructors() throws IOException {
		try {
			this.getInvernoCompiler().compile(MODULEL);
			Assertions.fail("Should throw an InvernoCompilationException");
		}
		catch(InvernoCompilationException e) {
			Assertions.assertEquals(1, e.getDiagnostics().size());
			
			String multipleSocketConstructorsError = "No constructor annotated with BeanSocket is enabled in module bean io.inverno.core.test.error.moduleL:beanA, consider enabling one constructor";
			
			Assertions.assertTrue(e.getDiagnostics().stream().map(d -> d.getMessage(Locale.getDefault())).collect(Collectors.toList()).containsAll(List.of(multipleSocketConstructorsError)));
		}
	}
}
