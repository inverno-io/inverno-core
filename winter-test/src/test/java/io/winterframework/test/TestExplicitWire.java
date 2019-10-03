package io.winterframework.test;

import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import io.winterframework.core.test.AbstractWinterTest;
import io.winterframework.core.test.WinterCompilationException;
import io.winterframework.core.test.WinterModuleProxy;

public class TestExplicitWire extends AbstractWinterTest {

	private static final String MODULEA = "io.winterframework.test.explicitwire.moduleA";
	private static final String MODULEB = "io.winterframework.test.explicitwire.moduleB";
	private static final String MODULEC = "io.winterframework.test.explicitwire.moduleC";
	private static final String MODULED = "io.winterframework.test.explicitwire.moduleD";
	
	@Test
	public void testSimpleWire() throws IllegalArgumentException, IllegalAccessException, NoSuchFieldException, SecurityException, IOException, WinterCompilationException {
		WinterModuleProxy moduleProxy = this.getWinterCompiler().compile(MODULEA).load(MODULEA).build();
		
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
	public void testFullyQualifiedWire() throws IllegalArgumentException, IllegalAccessException, NoSuchFieldException, SecurityException, IOException, WinterCompilationException {
		WinterModuleProxy moduleProxy = this.getWinterCompiler().compile(MODULEA).load(MODULEA).build();
		
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
	public void testMultiWire() throws IllegalArgumentException, IllegalAccessException, NoSuchFieldException, SecurityException, IOException, WinterCompilationException {
		WinterModuleProxy moduleProxy = this.getWinterCompiler().compile(MODULEA).load(MODULEA).build();
		
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
	public void testUnknownBean() throws IllegalArgumentException, IllegalAccessException, NoSuchFieldException, SecurityException, IOException {
		try {
			this.getWinterCompiler().compile(MODULEB);
			Assertions.fail("Should throw a WinterCompilationException");
		}
		catch(WinterCompilationException e) {
			Assertions.assertEquals(3, e.getDiagnotics().size());
			
			String unknnownBeansMessage = "Unkown beans: io.winterframework.test.explicitwire.moduleB:service1";
			String unknownSocketMessage = "There's no socket bean named: beanA:service";
			String conflictMessage = "Multiple beans matching socket io.winterframework.test.explicitwire.moduleB:beanB:service were found\n" +
					"  - io.winterframework.test.explicitwire.moduleB:service3 of type io.winterframework.test.explicitwire.moduleB.Service3\n" +
					"  - io.winterframework.test.explicitwire.moduleB:service2 of type io.winterframework.test.explicitwire.moduleB.Service2\n" +
					"  \n" + 
					"  Consider specifying an explicit wiring in module io.winterframework.test.explicitwire.moduleB (eg. @io.winterframework.core.annotation.Wire(beans=\"io.winterframework.test.explicitwire.moduleB:service3\", into=\"io.winterframework.test.explicitwire.moduleB:beanB:service\") )\n" +
					"   ";
			
			Assertions.assertTrue(e.getDiagnotics().stream().map(d -> d.getMessage(Locale.getDefault())).collect(Collectors.toList()).containsAll(List.of(unknnownBeansMessage, unknownSocketMessage, conflictMessage)));
		}
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Test
	public void testMultiModuleWire() throws IOException, WinterCompilationException, IllegalArgumentException, IllegalAccessException, NoSuchFieldException, SecurityException {
		WinterModuleProxy moduleProxy = this.getWinterCompiler().compile(MODULEC, MODULED).load(MODULEC).build();
		
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
}
