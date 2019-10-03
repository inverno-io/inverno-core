package io.winterframework.test;

import java.io.IOException;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import io.winterframework.core.test.AbstractWinterTest;
import io.winterframework.core.test.WinterCompilationException;
import io.winterframework.core.test.WinterModuleProxy;

public class TestSimplebean extends AbstractWinterTest {

	private static final String MODULE = "io.winterframework.test.simplebean";
	
	@Test
	public void testBeanCreation() throws IOException, WinterCompilationException {
		WinterModuleProxy simpleProxy = this.getWinterCompiler().compile(MODULE).load(MODULE).build();
		
		simpleProxy.start();
		Assertions.assertNotNull(simpleProxy.getBean("beanA"));
		Assertions.assertEquals(simpleProxy.getBean("beanA"), simpleProxy.getBean("beanA"));
		simpleProxy.stop();
	}

}
