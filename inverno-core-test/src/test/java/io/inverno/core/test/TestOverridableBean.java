/*
 * Copyright 2020 Jeremy KUHN
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
import java.lang.reflect.InvocationTargetException;
import java.util.function.Supplier;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import io.inverno.test.InvernoCompilationException;
import io.inverno.test.InvernoModuleLoader;
import io.inverno.test.InvernoModuleProxy;
import io.inverno.test.InvernoModuleProxyBuilder;

/**
 * @author <a href="mailto:jeremy.kuhn@inverno.io">Jeremy Kuhn</a>
 *
 */
public class TestOverridableBean extends AbstractCoreInvernoTest {

	private static final String MODULEA = "io.inverno.core.test.overridable.moduleA";
	private static final String MODULEB = "io.inverno.core.test.overridable.moduleB";
	
	@Test
	public void testModuleBeanOverride() throws IOException, InvernoCompilationException, IllegalArgumentException, IllegalAccessException, NoSuchFieldException, SecurityException, ClassNotFoundException, InstantiationException, InvocationTargetException, NoSuchMethodException {
		InvernoModuleLoader moduleLoader = this.getInvernoCompiler().compile(MODULEA);
		InvernoModuleProxyBuilder moduleProxyBuilder = moduleLoader.load(MODULEA);
		InvernoModuleProxy moduleA = moduleProxyBuilder.build();
		moduleA.start();
		try {
			Object beanA = moduleA.getBean("beanA");
			Assertions.assertNotNull(beanA);
			
			Object beanA_value = beanA.getClass().getField("value").get(beanA);
			Assertions.assertNotNull(beanA_value);
			
			Assertions.assertEquals("non-overridden", beanA_value);
			
			Object bean = moduleA.getBean("bean");
			Assertions.assertNotNull(bean);
			
			Object bean_beanA = bean.getClass().getField("beanA").get(bean);
			Assertions.assertNotNull(bean_beanA);
			
			Assertions.assertEquals(beanA, bean_beanA);
		}
		finally {
			moduleA.stop();
		}
		
		Class<?> beanAClass = moduleLoader.loadClass(MODULEA, "io.inverno.core.test.overridable.moduleA.BeanA");
		Object overriddenBeanA = beanAClass.getConstructor(String.class).newInstance("overridden");
		
		moduleA = moduleProxyBuilder.optionalDependency("beanA", overriddenBeanA).build();
		moduleA.start();
		try {
			Object beanA = moduleA.getBean("beanA");
			Assertions.assertNotNull(beanA);
			
			Object beanA_value = beanA.getClass().getField("value").get(beanA);
			Assertions.assertNotNull(beanA_value);
			
			Assertions.assertEquals("overridden", beanA_value);
			
			Object bean = moduleA.getBean("bean");
			Assertions.assertNotNull(bean);
			
			Object bean_beanA = bean.getClass().getField("beanA").get(bean);
			Assertions.assertNotNull(bean_beanA);
			
			Assertions.assertEquals(beanA, bean_beanA);
		}
		finally {
			moduleA.stop();
		}
	}
	
	@SuppressWarnings("unchecked")
	@Test
	public void testWrapperBeanOverride() throws IOException, InvernoCompilationException, IllegalArgumentException, IllegalAccessException, NoSuchFieldException, SecurityException {
		InvernoModuleLoader moduleLoader = this.getInvernoCompiler().compile(MODULEB);
		InvernoModuleProxyBuilder moduleProxyBuilder = moduleLoader.load(MODULEB);
		InvernoModuleProxy moduleB = moduleProxyBuilder.build();
		moduleB.start();
		try {
			Object beanB = moduleB.getBean("beanB");
			Assertions.assertNotNull(beanB);
			
			Assertions.assertEquals("non-overridden", ((Supplier<String>)beanB).get());
			
			Object bean = moduleB.getBean("bean");
			Assertions.assertNotNull(bean);
			
			Object bean_beanB = bean.getClass().getField("beanB").get(bean);
			Assertions.assertNotNull(bean_beanB);
			
			Assertions.assertEquals(beanB, bean_beanB);
		}
		finally {
			moduleB.stop();
		}
		
		Supplier<String> overriddenBeanB = () -> "overridden";
		moduleB = moduleProxyBuilder.optionalDependency("beanB", overriddenBeanB).build();
		moduleB.start();
		try {
			Object beanB = moduleB.getBean("beanB");
			Assertions.assertNotNull(beanB);
			
			Assertions.assertEquals("overridden", ((Supplier<String>)beanB).get());
			
			Object bean = moduleB.getBean("bean");
			Assertions.assertNotNull(bean);
			
			Object bean_beanB = bean.getClass().getField("beanB").get(bean);
			Assertions.assertNotNull(bean_beanB);
			
			Assertions.assertEquals(beanB, bean_beanB);
		}
		finally {
			moduleB.stop();
		}
	}
}
