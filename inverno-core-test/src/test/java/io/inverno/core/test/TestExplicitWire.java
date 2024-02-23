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
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import io.inverno.test.InvernoCompilationException;
import io.inverno.test.InvernoModuleProxy;

/**
 * 
 * @author <a href="mailto:jeremy.kuhn@inverno.io">Jeremy Kuhn</a>
 *
 */
public class TestExplicitWire extends AbstractCoreInvernoTest {

	private static final String MODULEA = "io.inverno.core.test.explicitwire.moduleA";
	private static final String MODULEB = "io.inverno.core.test.explicitwire.moduleB";
	private static final String MODULEC = "io.inverno.core.test.explicitwire.moduleC";
	private static final String MODULED = "io.inverno.core.test.explicitwire.moduleD";
	private static final String MODULEE = "io.inverno.core.test.explicitwire.moduleE";
	private static final String MODULEF = "io.inverno.core.test.explicitwire.moduleF";
	private static final String MODULEG = "io.inverno.core.test.explicitwire.moduleG";
	private static final String MODULEH = "io.inverno.core.test.explicitwire.moduleH";
	private static final String MODULEI = "io.inverno.core.test.explicitwire.moduleI";
	private static final String MODULEJ = "io.inverno.core.test.explicitwire.moduleJ";
	
	@Test
	public void testSimpleWire() throws IllegalArgumentException, IllegalAccessException, NoSuchFieldException, SecurityException, IOException, InvernoCompilationException {
		InvernoModuleProxy moduleProxy = this.getInvernoCompiler().compile(MODULEA).load(MODULEA).build();
		
		moduleProxy.start();
		
		Object beanA = moduleProxy.getBean("beanA");
		Object service1 = moduleProxy.getBean("service1");
		
		Assertions.assertNotNull(beanA);
		Assertions.assertNotNull(service1);
		
		Object beanA_service = beanA.getClass().getField("service").get(beanA);

		Assertions.assertNotNull(beanA_service);
		
		Assertions.assertEquals(service1, beanA_service);
		
		moduleProxy.stop();
	}
	
	@Test
	public void testFullyQualifiedWire() throws IllegalArgumentException, IllegalAccessException, NoSuchFieldException, SecurityException, IOException, InvernoCompilationException {
		InvernoModuleProxy moduleProxy = this.getInvernoCompiler().compile(MODULEA).load(MODULEA).build();
		
		moduleProxy.start();
		
		Object beanB = moduleProxy.getBean("beanB");
		Object service2 = moduleProxy.getBean("service2");
		
		Assertions.assertNotNull(beanB);
		Assertions.assertNotNull(service2);
		
		Object beanB_service = beanB.getClass().getField("service").get(beanB);

		Assertions.assertNotNull(beanB_service);
		Assertions.assertEquals(service2, beanB_service);
		
		moduleProxy.stop();
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Test
	public void testMultiWire() throws IllegalArgumentException, IllegalAccessException, NoSuchFieldException, SecurityException, IOException, InvernoCompilationException {
		InvernoModuleProxy moduleProxy = this.getInvernoCompiler().compile(MODULEA).load(MODULEA).build();
		
		moduleProxy.start();
		
		Object beanC = moduleProxy.getBean("beanC");
		Object service1 = moduleProxy.getBean("service1");
		Object service3 = moduleProxy.getBean("service3");
		
		Assertions.assertNotNull(beanC);
		Assertions.assertNotNull(service1);
		Assertions.assertNotNull(service3);
		
		Object beanC_services = beanC.getClass().getField("services").get(beanC);

		Assertions.assertNotNull(beanC_services);
		Assertions.assertEquals(2, ((List)beanC_services).size());
		Assertions.assertTrue(((List)beanC_services).containsAll(List.of(service1, service3)));
		
		moduleProxy.stop();
	}
	
	@Test
	public void testSimpleUnkownBeansAndSockets() throws IllegalArgumentException, IllegalAccessException, NoSuchFieldException, SecurityException, IOException {
		try {
			this.getInvernoCompiler().compile(MODULEB);
			Assertions.fail("Should throw an InvernoCompilationException");
		}
		catch(InvernoCompilationException e) {
			Assertions.assertEquals(3, e.getDiagnostics().size());
			
			String noService1Message = "There's no bean named io.inverno.core.test.explicitwire.moduleB:service1";
			String noSocketMessage = "There's no socket named: beanA:service";
			String noUnkownMessage = "There's no bean named io.inverno.core.test.explicitwire.moduleB:unkown that can be wired to io.inverno.core.test.explicitwire.moduleB:beanB:service";
			/*String conflictMessage = "Multiple beans matching socket io.inverno.core.test.explicitwire.moduleB:beanB:service were found\n" +
				"  - io.inverno.core.test.explicitwire.moduleB:service3 of type io.inverno.core.test.explicitwire.moduleB.Service3\n" +
				"  - io.inverno.core.test.explicitwire.moduleB:service2 of type io.inverno.core.test.explicitwire.moduleB.Service2\n" +
				"  \n" + 
				"  Consider specifying an explicit wiring in module io.inverno.core.test.explicitwire.moduleB (eg. @io.inverno.core.annotation.Wire(beans=\"io.inverno.core.test.explicitwire.moduleB:service3\", into=\"io.inverno.core.test.explicitwire.moduleB:beanB:service\") )\n" +
				"   ";*/
			
			Assertions.assertTrue(e.getDiagnostics().stream().map(d -> d.getMessage(Locale.getDefault())).collect(Collectors.toList()).containsAll(List.of(noService1Message, noSocketMessage, noUnkownMessage/*, conflictMessage*/)));
		}
	}
	
	@Test
	public void testMultiUnkownBeansAndSockets() throws IllegalArgumentException, IllegalAccessException, NoSuchFieldException, SecurityException, IOException {
		try {
			this.getInvernoCompiler().compile(MODULEE);
			Assertions.fail("Should throw an InvernoCompilationException");
		}
		catch(InvernoCompilationException e) {
			Assertions.assertEquals(4, e.getDiagnostics().size());
			
			String noService1Message = "There's no bean named io.inverno.core.test.explicitwire.moduleE:service1";
			String noSocketMessage = "There's no socket named: beanA:service";
			String noService4Message = "There's no bean named io.inverno.core.test.explicitwire.moduleE:service4 that can be wired to io.inverno.core.test.explicitwire.moduleE:beanE:services";
			String noUnkownMessage = "There's no bean named io.inverno.core.test.explicitwire.moduleE:unkown that can be wired to io.inverno.core.test.explicitwire.moduleE:beanE:services";
			
			Assertions.assertTrue(e.getDiagnostics().stream().map(d -> d.getMessage(Locale.getDefault())).collect(Collectors.toList()).containsAll(List.of(noService1Message, noSocketMessage, noService4Message, noUnkownMessage)));
		}
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Test
	public void testMultiModuleWire() throws IOException, InvernoCompilationException, IllegalArgumentException, IllegalAccessException, NoSuchFieldException, SecurityException {
		this.getInvernoCompiler().cleanModuleTarget();
		InvernoModuleProxy moduleProxy = this.getInvernoCompiler().compile(MODULEC, MODULED).load(MODULEC).build();
		
		moduleProxy.start();
		
		Object beanC = moduleProxy.getBean("beanC");
		Object runnable1 = moduleProxy.getBean("runnable1");
		Object runnable2 = moduleProxy.getBean("runnable2");
		Object runnable3 = moduleProxy.getBean("runnable3");
		
		Assertions.assertNotNull(beanC);
		Assertions.assertNotNull(runnable1);
		Assertions.assertNotNull(runnable2);
		Assertions.assertNotNull(runnable3);
		
		Object beanC_beanD = beanC.getClass().getField("beanD").get(beanC);
		Object beanC_callable1 = beanC.getClass().getField("callable1").get(beanC);
		Object beanC_callable2 = beanC.getClass().getField("callable2").get(beanC);
		Object beanC_callables = beanC.getClass().getField("callables").get(beanC);

		Assertions.assertNotNull(beanC_beanD);
		Assertions.assertNotNull(beanC_callable1);
		Assertions.assertNotNull(beanC_callable2);
		Assertions.assertNotNull(beanC_callables);
		
		Object beanC_beanD_runnable1 = beanC_beanD.getClass().getField("runnable1").get(beanC_beanD);
		Object beanC_beanD_runnable2 = beanC_beanD.getClass().getField("runnable2").get(beanC_beanD);
		Object beanC_beanD_runnables = beanC_beanD.getClass().getField("runnables").get(beanC_beanD);
		Object beanC_beanD_callableA = beanC_beanD.getClass().getField("callableA").get(beanC_beanD);
		Object beanC_beanD_callableB = beanC_beanD.getClass().getField("callableB").get(beanC_beanD);
		Object beanC_beanD_callableC = beanC_beanD.getClass().getField("callableC").get(beanC_beanD);
		
		Assertions.assertNotNull(beanC_beanD_runnable1);
		Assertions.assertNotNull(beanC_beanD_runnable2);
		Assertions.assertNotNull(beanC_beanD_runnables);
		Assertions.assertNotNull(beanC_beanD_callableA);
		Assertions.assertNotNull(beanC_beanD_callableB);
		Assertions.assertNotNull(beanC_beanD_callableC);
		
		Assertions.assertEquals(beanC_beanD_callableA, beanC_callable1);
		Assertions.assertEquals(beanC_beanD_callableB, beanC_callable2);
		Assertions.assertEquals(2, ((List)beanC_callables).size());
		Assertions.assertTrue(((List)beanC_callables).containsAll(List.of(beanC_beanD_callableA, beanC_beanD_callableC)));
		
		Assertions.assertEquals(runnable1, beanC_beanD_runnable1);
		Assertions.assertEquals(runnable2, beanC_beanD_runnable2);
		Assertions.assertEquals(2, ((List)beanC_beanD_runnables).size());
		Assertions.assertTrue(((List)beanC_beanD_runnables).containsAll(List.of(runnable1, runnable3)));
		
		moduleProxy.stop();
	}
	
	@Test
	public void testWireIntoBeanSocketSameModule() throws IOException {
		try {
			this.getInvernoCompiler().compile(MODULEF);
			Assertions.fail("Should throw an InvernoCompilationException");
		}
		catch(InvernoCompilationException e) {
			Assertions.assertEquals(2, e.getDiagnostics().size());
			
			String wireBeanToSocketSameModuleMessage = "You can't wire beans to a socket bean defined in the same module";
			String unwiredSocketMessage = "Ignoring socket bean which is not wired";
			
			Assertions.assertTrue(e.getDiagnostics().stream().map(d -> d.getMessage(Locale.getDefault())).collect(Collectors.toList()).containsAll(List.of(wireBeanToSocketSameModuleMessage, unwiredSocketMessage)));
		}
	}
	
	@Test
	public void testInvalidSocketQualifiedName() throws IOException {
		try {
			this.getInvernoCompiler().compile(MODULEG);
			Assertions.fail("Should throw an InvernoCompilationException");
		}
		catch(InvernoCompilationException e) {
			Assertions.assertEquals(2, e.getDiagnostics().size());
			
			String invalidSocketQNameMessage = "Invalid socket qualified name: test, expecting (<moduleName>):<beanName>:<socketName> OR <moduleName>:<beanName> with valid Java identifiers";
			String invalidBeanQNameMessage = "Invalid bean qualified name: #bad, expecting (<moduleName>):<beanName> with valid Java identifiers";
			/*String conflictMessage = "Multiple beans matching socket io.inverno.core.test.explicitwire.moduleG:beanG:runnable were found\n" +
				"  - io.inverno.core.test.explicitwire.moduleG:runnable2 of type io.inverno.core.test.explicitwire.moduleG.Runnable2\n" +
				"  - io.inverno.core.test.explicitwire.moduleG:runnable1 of type io.inverno.core.test.explicitwire.moduleG.Runnable1\n" +
				"  \n" + 
				"  Consider specifying an explicit wiring in module io.inverno.core.test.explicitwire.moduleG (eg. @io.inverno.core.annotation.Wire(beans=\"io.inverno.core.test.explicitwire.moduleG:runnable2\", into=\"io.inverno.core.test.explicitwire.moduleG:beanG:runnable\") )\n" +
				"   ";*/
			
			Assertions.assertTrue(e.getDiagnostics().stream().map(d -> d.getMessage(Locale.getDefault())).collect(Collectors.toList()).containsAll(List.of(invalidSocketQNameMessage, invalidBeanQNameMessage/*, conflictMessage*/)));
		}
	}
	
	@Test
	public void testMultipleBeansWithName() throws IOException {
		try {
			this.getInvernoCompiler().compile(MODULEH);
			Assertions.fail("Should throw an InvernoCompilationException");
		}
		catch(InvernoCompilationException e) {
			Assertions.assertEquals(4, e.getDiagnostics().size());
			
			String multipleRunnable1Message = "Multiple beans with name runnable1 exist in module io.inverno.core.test.explicitwire.moduleH";
			String duplicateBeansWireMessage = "The following beans are specified multiple times: io.inverno.core.test.explicitwire.moduleH:runnable1";
			String cantWireSameNameMessage = "Can't wire different beans with same name io.inverno.core.test.explicitwire.moduleH:runnable1 into io.inverno.core.test.explicitwire.moduleH:beanH:runnables";

			Assertions.assertEquals(2, Collections.frequency(e.getDiagnostics().stream().map(d -> d.getMessage(Locale.getDefault())).collect(Collectors.toList()), multipleRunnable1Message));
			Assertions.assertTrue(e.getDiagnostics().stream().map(d -> d.getMessage(Locale.getDefault())).collect(Collectors.toList()).containsAll(List.of(multipleRunnable1Message, duplicateBeansWireMessage, cantWireSameNameMessage)));
		}
	}
	
	@Test
	public void testMultipleWiresTargetingSocket() throws IOException {
		try {
			this.getInvernoCompiler().compile(MODULEI);
			Assertions.fail("Should throw an InvernoCompilationException");
		}
		catch(InvernoCompilationException e) {
			Assertions.assertEquals(2, e.getDiagnostics().size());
			
			String multipleWiresMessage = "Multiple wires targeting socket io.inverno.core.test.explicitwire.moduleI:beanI:runnables were found";

			Assertions.assertEquals(2, Collections.frequency(e.getDiagnostics().stream().map(d -> d.getMessage(Locale.getDefault())).collect(Collectors.toList()), multipleWiresMessage));
		}
	}
	
	@Test
	public void testMultipleBeansSingleSocket() throws IOException {
		try {
			this.getInvernoCompiler().compile(MODULEJ);
			Assertions.fail("Should throw an InvernoCompilationException");
		}
		catch(InvernoCompilationException e) {
			Assertions.assertEquals(1, e.getDiagnostics().size());
			
			String cantWireMultipleBeansInSingleSocketMessage = "Can't wire multiple beans in single socket io.inverno.core.test.explicitwire.moduleJ:beanJ:runnable";
			
			Assertions.assertTrue(e.getDiagnostics().stream().map(d -> d.getMessage(Locale.getDefault())).collect(Collectors.toList()).containsAll(List.of(cantWireMultipleBeansInSingleSocketMessage)));
		}
	}
}
