package io.winterframework.test;

import java.io.IOException;
import java.util.Set;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import io.winterframework.core.test.AbstractWinterTest;
import io.winterframework.core.test.WinterCompilationException;
import io.winterframework.core.test.WinterModuleProxy;

public class TestSelfWire extends AbstractWinterTest {

	private static final String SELFWIRE_MODULE = "io.winterframework.test.selfwire";
	
	private WinterModuleProxy moduleProxy;
	
	@BeforeEach
	public void init() throws IOException, WinterCompilationException {
		if(this.moduleProxy == null) {
			this.moduleProxy = this.getWinterCompiler().compile(SELFWIRE_MODULE).load(SELFWIRE_MODULE).build();
		}
	}
	
	@Test
	public void testSingleSelfWire() throws IllegalArgumentException, IllegalAccessException, NoSuchFieldException, SecurityException {
		this.moduleProxy.start();
			
		try {
			Object serviceC = this.moduleProxy.getBean("serviceC");
			Object serviceD = this.moduleProxy.getBean("serviceD");
			
			Assertions.assertNotNull(serviceC);
			Assertions.assertNotNull(serviceD);
			
			Object serviceC_service = serviceC.getClass().getField("service").get(serviceC);
			
			Assertions.assertNotNull(serviceC_service);
			Assertions.assertEquals(serviceD, serviceC_service);
		}
		finally {
			this.moduleProxy.stop();
		}
	}
	
	@Test
	public void testMultiSelfWire() throws IllegalArgumentException, IllegalAccessException, NoSuchFieldException, SecurityException {
		this.moduleProxy.start();
		
		try {
			Object metaService = this.moduleProxy.getBean("metaService");
			Object serviceA = this.moduleProxy.getBean("serviceA");
			Object serviceB = this.moduleProxy.getBean("serviceB");
			
			Assertions.assertNotNull(metaService);
			Assertions.assertNotNull(serviceA);
			Assertions.assertNotNull(serviceB);
			
			@SuppressWarnings("unchecked")
			Set<Object> metaService_services = (Set<Object>)metaService.getClass().getField("services").get(metaService);
			
			Assertions.assertEquals(2, metaService_services.size());
			Assertions.assertTrue(metaService_services.containsAll(Set.of(serviceA, serviceB)));
		}
		finally {
			this.moduleProxy.stop();
		}
	}
}
