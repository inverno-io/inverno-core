package io.winterframework.test;

import java.io.IOException;
import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import io.winterframework.core.test.AbstractWinterTest;
import io.winterframework.core.test.WinterCompilationException;
import io.winterframework.core.test.WinterModuleProxy;

public class TestManualWire extends AbstractWinterTest {

	private static final String MANUALWIRE_MODULE = "io.winterframework.test.manualwire";
	
	private WinterModuleProxy moduleProxy;
	
	@BeforeEach
	public void init() throws IOException, WinterCompilationException {
		if(this.moduleProxy == null) {
			this.moduleProxy = this.getWinterCompiler().compile(MANUALWIRE_MODULE).load(MANUALWIRE_MODULE).build();
		}
	}
	
	@Test
	public void testSimpleWire() throws IllegalArgumentException, IllegalAccessException, NoSuchFieldException, SecurityException {
		this.moduleProxy.start();
		
		Object beanA = this.moduleProxy.getBean("beanA");
		Object service1 = this.moduleProxy.getBean("service1");
		
		Assertions.assertNotNull(beanA);
		Assertions.assertNotNull(service1);
		
		Object beanA_service = beanA.getClass().getField("service").get(beanA);

		Assertions.assertNotNull(beanA_service);
		
		Assertions.assertEquals(service1, beanA_service);
		
		this.moduleProxy.stop();
	}
	
	@Test
	public void testFullyQualifiedWire() throws IllegalArgumentException, IllegalAccessException, NoSuchFieldException, SecurityException {
		this.moduleProxy.start();
		
		Object beanB = this.moduleProxy.getBean("beanB");
		Object service2 = this.moduleProxy.getBean("service2");
		
		Assertions.assertNotNull(beanB);
		Assertions.assertNotNull(service2);
		
		Object beanB_service = beanB.getClass().getField("service").get(beanB);

		Assertions.assertNotNull(beanB_service);
		Assertions.assertEquals(service2, beanB_service);
		
		this.moduleProxy.stop();
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Test
	public void testMultiWire() throws IllegalArgumentException, IllegalAccessException, NoSuchFieldException, SecurityException {
		this.moduleProxy.start();
		
		Object beanC = this.moduleProxy.getBean("beanC");
		Object service1 = this.moduleProxy.getBean("service1");
		Object service3 = this.moduleProxy.getBean("service3");
		
		Assertions.assertNotNull(beanC);
		Assertions.assertNotNull(service1);
		Assertions.assertNotNull(service3);
		
		Object beanC_services = beanC.getClass().getField("services").get(beanC);

		Assertions.assertNotNull(beanC_services);
		Assertions.assertEquals(2, ((List)beanC_services).size());
		Assertions.assertTrue(((List)beanC_services).containsAll(List.of(service1, service3)));
		
		this.moduleProxy.stop();
	}
}
