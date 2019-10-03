package io.winterframework.test;

import java.io.IOException;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import io.winterframework.core.test.AbstractWinterTest;
import io.winterframework.core.test.WinterCompilationException;
import io.winterframework.core.test.WinterModuleProxy;

public class TestScope extends AbstractWinterTest {

	private static final String MODULE = "io.winterframework.test.scope";
	
	private WinterModuleProxy scopeModuleProxy;
	
	@BeforeEach
	public void init() throws IOException, WinterCompilationException {
		if(this.scopeModuleProxy == null) {
			this.scopeModuleProxy = this.getWinterCompiler().compile(MODULE).load(MODULE).build();
		}
	}
	
	@Test
	public void testDefaultScope() {
		try {
			this.scopeModuleProxy.start();
			Assertions.assertNotNull(this.scopeModuleProxy.getBean("defaultScopeBean"));
			Assertions.assertEquals(this.scopeModuleProxy.getBean("defaultScopeBean"), this.scopeModuleProxy.getBean("defaultScopeBean"));
		}
		finally {
			this.scopeModuleProxy.stop();
		}
	}
	
	@Test
	public void testSingletonScope() {
		try {
			this.scopeModuleProxy.start();
			Assertions.assertNotNull(this.scopeModuleProxy.getBean("singletonScopeBean"));
			Assertions.assertEquals(this.scopeModuleProxy.getBean("singletonScopeBean"), this.scopeModuleProxy.getBean("singletonScopeBean"));
		}
		finally {
			this.scopeModuleProxy.stop();
		}
	}
	
	@Test
	public void testPrototypeScope() {
		try {
			this.scopeModuleProxy.start();
			Assertions.assertNotNull(this.scopeModuleProxy.getBean("prototypeScopeBean"));
			Assertions.assertNotEquals(this.scopeModuleProxy.getBean("prototypeScopeBean"), this.scopeModuleProxy.getBean("prototypeScopeBean"));
		}
		finally {
			this.scopeModuleProxy.stop();
		}
	}
}
