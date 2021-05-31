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
import java.util.Set;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import io.inverno.test.InvernoCompilationException;
import io.inverno.test.InvernoModuleProxy;

/**
 * 
 * @author <a href="mailto:jeremy.kuhn@inverno.io">Jeremy Kuhn</a>
 *
 */
public class TestSelfWire extends AbstractCoreInvernoTest {

	private static final String MODULE = "io.inverno.core.test.selfwire";
	
	private InvernoModuleProxy moduleProxy;
	
	@BeforeEach
	public void init() throws IOException, InvernoCompilationException {
		if(this.moduleProxy == null) {
			this.moduleProxy = this.getInvernoCompiler().compile(MODULE).load(MODULE).build();
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
