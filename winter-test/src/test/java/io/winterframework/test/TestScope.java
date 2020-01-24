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

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import io.winterframework.core.test.AbstractWinterTest;
import io.winterframework.core.test.WinterCompilationException;
import io.winterframework.core.test.WinterModuleProxy;

/**
 * 
 * @author jkuhn
 *
 */
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
