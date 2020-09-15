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
import java.lang.invoke.MethodHandles.Lookup;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import io.winterframework.core.test.AbstractWinterTest;
import io.winterframework.core.test.WinterCompilationException;
import io.winterframework.core.test.WinterModuleException;
import io.winterframework.core.test.WinterModuleLoader;
import io.winterframework.core.test.WinterModuleProxy;

/**
 * 
 * @author jkuhn
 *
 */
public class TestConfiguration extends AbstractWinterTest {

	private static final String MODULEA = "io.winterframework.test.config.moduleA";
	private static final String MODULEB = "io.winterframework.test.config.moduleB";
	private static final String MODULEC = "io.winterframework.test.config.moduleC";
	private static final String MODULED = "io.winterframework.test.config.moduleD";

	private static class ConfigurationInvocationHandler implements InvocationHandler {
		
		private Class<?> configurationClass;
		
		private Map<String, Object> properties;
		
		public ConfigurationInvocationHandler(Class<?> configurationClass, Map<String, Object> properties) {
			this.configurationClass = configurationClass;
			this.properties = new HashMap<>(properties);
		}
		
		@Override
		public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
			if(method.getName().equals("equals")) {
				if (args[0] == null) {
	                return false;
	            }
	            InvocationHandler handler = Proxy.getInvocationHandler(args[0]);
	            if (!(handler instanceof ConfigurationInvocationHandler)) {
	                return false;
	            }
	            ConfigurationInvocationHandler configurationHandler = (ConfigurationInvocationHandler)handler;
	            if(!configurationHandler.configurationClass.equals(this.configurationClass)) {
	            	return false;
	            }
	            return configurationHandler.properties.equals(this.properties);
			}
			if(this.properties.containsKey(method.getName())) {
				return this.properties.get(method.getName());
			}
			else if(method.isDefault()) {
				Constructor<Lookup> constructor = Lookup.class.getDeclaredConstructor(Class.class);
				constructor.setAccessible(true);
				this.properties.put(method.getName(), constructor.newInstance(configurationClass)
					.in(configurationClass)
					.unreflectSpecial(method, configurationClass)
					.bindTo(proxy)
					.invokeWithArguments());
				
				/*return MethodHandles.lookup()
                .in(this.configurationClass)
                .unreflectSpecial(method, this.configurationClass)
                .bindTo(proxy)
                .invokeWithArguments();*/
				
//			    MethodHandle handle = MethodHandles.lookup().findSpecial(this.configurationClass, method.getName(), MethodType.methodType(method.getReturnType(), method.getParameterTypes()), this.configurationClass).bindTo(proxy);
//			    return handle.invokeWithArguments();
			}
			return this.properties.get(method.getName());
		}
	}
	
	private Object getProperty(Object config, String propertyName) {
		try {
			Method propertyMethod = config.getClass().getMethod(propertyName);
			propertyMethod.setAccessible(true);
			return propertyMethod.invoke(config);
		} catch (NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException
				| InvocationTargetException e) {
			throw new RuntimeException(e);
		}
	}
	
	private void setProperty(Object builder, String propertyName, Class<?> propertyType, Object value) {
		try {
			Method propertyMethod = builder.getClass().getMethod(propertyName, propertyType);
			propertyMethod.setAccessible(true);
			propertyMethod.invoke(builder, value);
		} catch (NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException
				| InvocationTargetException e) {
			throw new RuntimeException(e);
		}
	}
	
	@Test
	public void testConfiguration_none() throws IOException, WinterCompilationException, IllegalArgumentException, IllegalAccessException, NoSuchFieldException, SecurityException, InvocationTargetException, NoSuchMethodException {
		WinterModuleLoader moduleLoader = this.getWinterCompiler().compile(MODULEA);
		WinterModuleProxy moduleA = moduleLoader.load(MODULEA).build();
		moduleA.start();
		try {
			Object beanA = moduleA.getBean("beanA");
			Assertions.assertNotNull(beanA);
			
			Object beanA_configA = beanA.getClass().getField("configA").get(beanA);
			Assertions.assertNotNull(beanA);
			
			Assertions.assertNotNull(beanA_configA);
			
			Object beanA_configA_param1 = this.getProperty(beanA_configA, "param1");
			Assertions.assertNull(beanA_configA_param1);
			
			Object beanA_configA_param2 = this.getProperty(beanA_configA, "param2");
			Assertions.assertNotNull(beanA_configA_param2);
			Assertions.assertEquals(Integer.valueOf(53), beanA_configA_param2);
		}
		finally {
			moduleA.stop();
		}
	}
	
	@Test
	public void testConfiguration_builder() throws IOException, WinterCompilationException, IllegalArgumentException, IllegalAccessException, NoSuchFieldException, SecurityException, InvocationTargetException, NoSuchMethodException {
		WinterModuleLoader moduleLoader = this.getWinterCompiler().compile(MODULEA);
		
		Consumer<Object> configAConfigurator = builder -> {
			this.setProperty(builder, "param1", String.class, "test");
		};
		
		WinterModuleProxy moduleA = moduleLoader.load(MODULEA).optionalDependency("configA", Consumer.class, configAConfigurator).build();
		moduleA.start();
		try {
			Object beanA = moduleA.getBean("beanA");
			Assertions.assertNotNull(beanA);
			
			Object beanA_configA = beanA.getClass().getField("configA").get(beanA);
			Assertions.assertNotNull(beanA);
			
			Assertions.assertNotNull(beanA_configA);
			
			Object beanA_configA_param1 = this.getProperty(beanA_configA, "param1");
			Assertions.assertNotNull(beanA_configA_param1);
			Assertions.assertEquals("test", beanA_configA_param1);
			
			Object beanA_configA_param2 = this.getProperty(beanA_configA, "param2");
			Assertions.assertNotNull(beanA_configA_param2);
			Assertions.assertEquals(Integer.valueOf(53), beanA_configA_param2);
		}
		finally {
			moduleA.stop();
		}
	}
	
	@Test
	public void testConfiguration_impl() throws IOException, WinterCompilationException, IllegalArgumentException, IllegalAccessException, NoSuchFieldException, SecurityException, InvocationTargetException, NoSuchMethodException, ClassNotFoundException {
		WinterModuleLoader moduleLoader = this.getWinterCompiler().compile(MODULEA);
		
		Class<?> configAClass = moduleLoader.loadClass(MODULEA, "io.winterframework.test.config.moduleA.ConfigA");
		ConfigurationInvocationHandler configAHandler = new ConfigurationInvocationHandler(configAClass, Map.of("param1", "abcdef", "param2", 5));
		Object configA = Proxy.newProxyInstance(configAClass.getClassLoader(),
                new Class<?>[] { configAClass },
                configAHandler);
		
		WinterModuleProxy moduleA = moduleLoader.load(MODULEA).optionalDependency("configA", configAClass, configA).build();
		moduleA.start();
		try {
			Object beanA = moduleA.getBean("beanA");
			Assertions.assertNotNull(beanA);
			
			Object beanA_configA = beanA.getClass().getField("configA").get(beanA);
			Assertions.assertNotNull(beanA);
			
			Assertions.assertNotNull(beanA_configA);
			
			Object beanA_configA_param1 = this.getProperty(beanA_configA, "param1");
			Assertions.assertNotNull(beanA_configA_param1);
			Assertions.assertEquals("abcdef", beanA_configA_param1);
			
			Object beanA_configA_param2 = this.getProperty(beanA_configA, "param2");
			Assertions.assertNotNull(beanA_configA_param2);
			Assertions.assertEquals(Integer.valueOf(5), beanA_configA_param2);
		}
		finally {
			moduleA.stop();
		}
	}
	
	@Test
	public void testConfiguration_null() throws IOException, WinterCompilationException, IllegalArgumentException, IllegalAccessException, NoSuchFieldException, SecurityException, InvocationTargetException, NoSuchMethodException, ClassNotFoundException {
		WinterModuleLoader moduleLoader = this.getWinterCompiler().compile(MODULEA);
		
		Class<?> configAClass = moduleLoader.loadClass(MODULEA, "io.winterframework.test.config.moduleA.ConfigA");
		
		try {
			moduleLoader.load(MODULEA).optionalDependency("configA", configAClass, null).build();
			Assertions.fail("Should throw a WinterModuleException with a NullpointerException");
		} 
		catch (WinterModuleException e) {
			Assertions.assertTrue(e.getCause() instanceof NullPointerException);
			Assertions.assertNull(e.getCause().getMessage());
		}
	}
	
	@Test
	public void testConfigurationConflict() throws IOException, WinterCompilationException, IllegalArgumentException, IllegalAccessException, NoSuchFieldException, SecurityException, InvocationTargetException, NoSuchMethodException, ClassNotFoundException {
		try {
			this.getWinterCompiler().compile(MODULEB);
			Assertions.fail("Should throw a WinterCompilationException");
		}
		catch(WinterCompilationException e) {
			Assertions.assertEquals(2, e.getDiagnostics().size());
			
			String configurationBeanConflict = "Multiple beans matching socket io.winterframework.test.config.moduleB:beanB:configB were found\n" + 
					"  - io.winterframework.test.config.moduleB:configB_Bean of type io.winterframework.test.config.moduleB.ConfigB_Bean\n" + 
					"  - io.winterframework.test.config.moduleB:configB of type io.winterframework.test.config.moduleB.ConfigB\n" + 
					"  \n" + 
					"  Consider specifying an explicit wiring in module io.winterframework.test.config.moduleB (eg. @io.winterframework.core.annotation.Wire(beans=\"io.winterframework.test.config.moduleB:configB_Bean\", into=\"io.winterframework.test.config.moduleB:beanB:configB\") )\n" + 
					"   ";
			String unwiredConfigurationBean = "Ignoring socket bean which is not wired";
					
			Assertions.assertTrue(e.getDiagnostics().stream().map(d -> d.getMessage(Locale.getDefault())).collect(Collectors.toList()).containsAll(List.of(configurationBeanConflict, unwiredConfigurationBean)));
		}
	}
	
	@Test
	public void testNestedConfiguration_none_none() throws IOException, WinterCompilationException, IllegalArgumentException, IllegalAccessException, NoSuchFieldException, SecurityException {
		WinterModuleLoader moduleLoader = this.getWinterCompiler().compile(MODULEA, MODULEC);
		WinterModuleProxy moduleC = moduleLoader.load(MODULEC).build();
		moduleC.start();
		try {
			Object beanC = moduleC.getBean("beanC");
			Assertions.assertNotNull(beanC);
			
			Object beanC_configC = beanC.getClass().getField("configC").get(beanC);
			Assertions.assertNotNull(beanC_configC);
			
			Object beanC_configA = beanC.getClass().getField("configA").get(beanC);
			Assertions.assertNotNull(beanC_configA);
			
			Object beanC_configC_param1 = this.getProperty(beanC_configC, "param1");
			Assertions.assertNotNull(beanC_configC_param1);
			Assertions.assertEquals("abc", beanC_configC_param1);
			
			Object beanC_configC_configA = this.getProperty(beanC_configC, "configA");
			Assertions.assertNotNull(beanC_configC_configA);
			Assertions.assertEquals(beanC_configA, beanC_configC_configA);
			
			Object beanC_configA_param1 = this.getProperty(beanC_configA, "param1");
			Assertions.assertNull(beanC_configA_param1);
			
			Object beanC_configA_param2 = this.getProperty(beanC_configA, "param2");
			Assertions.assertNotNull(beanC_configA_param2);
			Assertions.assertEquals(Integer.valueOf(53), beanC_configA_param2);
		}
		finally {
			moduleC.stop();
		}
	}
	
	@Test
	public void testNestedConfiguration_builder_none() throws IOException, WinterCompilationException, IllegalArgumentException, IllegalAccessException, NoSuchFieldException, SecurityException {
		WinterModuleLoader moduleLoader = this.getWinterCompiler().compile(MODULEA, MODULEC);
		
		Consumer<Object> configCConfigurator = builder -> {
			this.setProperty(builder, "param1", String.class, "def");
		};
		
		WinterModuleProxy moduleC = moduleLoader.load(MODULEC).optionalDependency("configC", Consumer.class, configCConfigurator).build();
		moduleC.start();
		try {
			Object beanC = moduleC.getBean("beanC");
			Assertions.assertNotNull(beanC);
			
			Object beanC_configC = beanC.getClass().getField("configC").get(beanC);
			Assertions.assertNotNull(beanC_configC);
			
			Object beanC_configA = beanC.getClass().getField("configA").get(beanC);
			Assertions.assertNotNull(beanC_configA);
			
			Object beanC_configC_param1 = this.getProperty(beanC_configC, "param1");
			Assertions.assertNotNull(beanC_configC_param1);
			Assertions.assertEquals("def", beanC_configC_param1);
			
			Object beanC_configC_configA = this.getProperty(beanC_configC, "configA");
			Assertions.assertNotNull(beanC_configC_configA);
			Assertions.assertEquals(beanC_configA, beanC_configC_configA);
			
			Object beanC_configA_param1 = this.getProperty(beanC_configA, "param1");
			Assertions.assertNull(beanC_configA_param1);
			
			Object beanC_configA_param2 = this.getProperty(beanC_configA, "param2");
			Assertions.assertNotNull(beanC_configA_param2);
			Assertions.assertEquals(Integer.valueOf(53), beanC_configA_param2);
		}
		finally {
			moduleC.stop();
		}
	}
	
	@Test
	public void testNestedConfiguration_builder_builder() throws IOException, WinterCompilationException, IllegalArgumentException, IllegalAccessException, NoSuchFieldException, SecurityException {
		WinterModuleLoader moduleLoader = this.getWinterCompiler().compile(MODULEA, MODULEC);

		Consumer<Object> configAConfigurator = builder -> {
			this.setProperty(builder, "param1", String.class, "ghi");
			this.setProperty(builder, "param2", int.class, 421);
		};
		
		Consumer<Object> configCConfigurator = builder -> {
			this.setProperty(builder, "param1", String.class, "def");
			this.setProperty(builder, "configA", Consumer.class, configAConfigurator);
		};
		
		WinterModuleProxy moduleC = moduleLoader.load(MODULEC).optionalDependency("configC", Consumer.class, configCConfigurator).build();
		moduleC.start();
		try {
			Object beanC = moduleC.getBean("beanC");
			Assertions.assertNotNull(beanC);
			
			Object beanC_configC = beanC.getClass().getField("configC").get(beanC);
			Assertions.assertNotNull(beanC_configC);
			
			Object beanC_configA = beanC.getClass().getField("configA").get(beanC);
			Assertions.assertNotNull(beanC_configA);
			
			Object beanC_configC_param1 = this.getProperty(beanC_configC, "param1");
			Assertions.assertNotNull(beanC_configC_param1);
			Assertions.assertEquals("def", beanC_configC_param1);
			
			Object beanC_configC_configA = this.getProperty(beanC_configC, "configA");
			Assertions.assertNotNull(beanC_configC_configA);
			Assertions.assertEquals(beanC_configA, beanC_configC_configA);
			
			Object beanC_configA_param1 = this.getProperty(beanC_configA, "param1");
			Assertions.assertNotNull(beanC_configA_param1);
			Assertions.assertEquals("ghi", beanC_configA_param1);
			
			Object beanC_configA_param2 = this.getProperty(beanC_configA, "param2");
			Assertions.assertNotNull(beanC_configA_param2);
			Assertions.assertEquals(Integer.valueOf(421), beanC_configA_param2);
		}
		finally {
			moduleC.stop();
		}
	}
	
	@Test
	public void testNestedConfiguration_builder_impl() throws IOException, WinterCompilationException, IllegalArgumentException, IllegalAccessException, NoSuchFieldException, SecurityException, ClassNotFoundException {
		WinterModuleLoader moduleLoader = this.getWinterCompiler().compile(MODULEA, MODULEC);

		Class<?> configAClass = moduleLoader.loadClass(MODULEA, "io.winterframework.test.config.moduleA.ConfigA");
		ConfigurationInvocationHandler configAHandler = new ConfigurationInvocationHandler(configAClass, Map.of("param1", "abcdef", "param2", 5));
		Object configA = Proxy.newProxyInstance(configAClass.getClassLoader(),
            new Class<?>[] { configAClass },
            configAHandler);
		
		Consumer<Object> configCConfigurator = builder -> {
			this.setProperty(builder, "param1", String.class, "def");
			this.setProperty(builder, "configA", configAClass, configA);
		};
		
		WinterModuleProxy moduleC = moduleLoader.load(MODULEC).optionalDependency("configC", Consumer.class, configCConfigurator).build();
		moduleC.start();
		try {
			Object beanC = moduleC.getBean("beanC");
			Assertions.assertNotNull(beanC);
			
			Object beanC_configC = beanC.getClass().getField("configC").get(beanC);
			Assertions.assertNotNull(beanC_configC);
			
			Object beanC_configA = beanC.getClass().getField("configA").get(beanC);
			Assertions.assertNotNull(beanC_configA);
			
			Object beanC_configC_param1 = this.getProperty(beanC_configC, "param1");
			Assertions.assertNotNull(beanC_configC_param1);
			Assertions.assertEquals("def", beanC_configC_param1);
			
			Object beanC_configC_configA = this.getProperty(beanC_configC, "configA");
			Assertions.assertNotNull(beanC_configC_configA);
			Assertions.assertEquals(beanC_configA, beanC_configC_configA);
			
			Object beanC_configA_param1 = this.getProperty(beanC_configA, "param1");
			Assertions.assertNotNull(beanC_configA_param1);
			Assertions.assertEquals("abcdef", beanC_configA_param1);
			
			Object beanC_configA_param2 = this.getProperty(beanC_configA, "param2");
			Assertions.assertNotNull(beanC_configA_param2);
			Assertions.assertEquals(Integer.valueOf(5), beanC_configA_param2);
		}
		finally {
			moduleC.stop();
		}
	}
	
	@Test
	public void testNestedConfiguration_builder_null() throws IOException, WinterCompilationException, IllegalArgumentException, IllegalAccessException, NoSuchFieldException, SecurityException, ClassNotFoundException {
		WinterModuleLoader moduleLoader = this.getWinterCompiler().compile(MODULEA, MODULEC);
		
		Class<?> configAClass = moduleLoader.loadClass(MODULEA, "io.winterframework.test.config.moduleA.ConfigA");
		
		Consumer<Object> configCConfigurator = builder -> {
			this.setProperty(builder, "param1", String.class, "def");
			this.setProperty(builder, "configA", configAClass, null);
		};
		
		try {
			moduleLoader.load(MODULEC).optionalDependency("configC", Consumer.class, configCConfigurator).build().start();
			Assertions.fail("Should throw a WinterModuleException with a NullpointerException");
		} 
		catch (WinterModuleException e) {
			Assertions.assertTrue(e.getCause() instanceof NullPointerException);
			Assertions.assertEquals("configC.configA", e.getCause().getMessage());
		}
	}
	
	@Test
	public void testNestedConfiguration_impl_null() throws IOException, WinterCompilationException, IllegalArgumentException, IllegalAccessException, NoSuchFieldException, SecurityException, ClassNotFoundException {
		WinterModuleLoader moduleLoader = this.getWinterCompiler().compile(MODULEA, MODULEC);
		
		Class<?> configCClass = moduleLoader.loadClass(MODULEC, "io.winterframework.test.config.moduleC.ConfigC");
		ConfigurationInvocationHandler configCHandler = new ConfigurationInvocationHandler(configCClass, Map.of("param1", "def"));
		Object configC = Proxy.newProxyInstance(configCClass.getClassLoader(),
                new Class<?>[] { configCClass },
                configCHandler);
		
		try {
			moduleLoader.load(MODULEC).optionalDependency("configC", configCClass, configC).build().start();
			Assertions.fail("Should throw a WinterModuleException with a NullpointerException");
		} 
		catch (WinterModuleException e) {
			Assertions.assertTrue(e.getCause() instanceof NullPointerException);
			Assertions.assertEquals("configC.configA", e.getCause().getMessage());
		}
	}
	
	@Test
	public void testNestedConfiguration_impl_impl() throws IOException, WinterCompilationException, IllegalArgumentException, IllegalAccessException, NoSuchFieldException, SecurityException, ClassNotFoundException {
		WinterModuleLoader moduleLoader = this.getWinterCompiler().compile(MODULEA, MODULEC);
		
		Class<?> configAClass = moduleLoader.loadClass(MODULEA, "io.winterframework.test.config.moduleA.ConfigA");
		ConfigurationInvocationHandler configAHandler = new ConfigurationInvocationHandler(configAClass, Map.of("param1", "abcdef", "param2", 5));
		Object configA = Proxy.newProxyInstance(configAClass.getClassLoader(),
                new Class<?>[] { configAClass },
                configAHandler);
		
		Class<?> configCClass = moduleLoader.loadClass(MODULEC, "io.winterframework.test.config.moduleC.ConfigC");
		ConfigurationInvocationHandler configCHandler = new ConfigurationInvocationHandler(configCClass, Map.of("param1", "def", "configA", configA));
		Object configC = Proxy.newProxyInstance(configCClass.getClassLoader(),
                new Class<?>[] { configCClass },
                configCHandler);
		
		WinterModuleProxy moduleC = moduleLoader.load(MODULEC).optionalDependency("configC", configCClass, configC).build();
		moduleC.start();
		try {
			Object beanC = moduleC.getBean("beanC");
			Assertions.assertNotNull(beanC);
			
			Object beanC_configC = beanC.getClass().getField("configC").get(beanC);
			Assertions.assertNotNull(beanC_configC);
			
			Object beanC_configA = beanC.getClass().getField("configA").get(beanC);
			Assertions.assertNotNull(beanC_configA);
			
			Object beanC_configC_param1 = this.getProperty(beanC_configC, "param1");
			Assertions.assertNotNull(beanC_configC_param1);
			Assertions.assertEquals("def", beanC_configC_param1);
			
			Object beanC_configA_param1 = this.getProperty(beanC_configA, "param1");
			Assertions.assertNotNull(beanC_configA_param1);
			Assertions.assertEquals("abcdef", beanC_configA_param1);
			
			Object beanC_configA_param2 = this.getProperty(beanC_configA, "param2");
			Assertions.assertNotNull(beanC_configA_param2);
			Assertions.assertEquals(Integer.valueOf(5), beanC_configA_param2);
			
			Object beanC_configC_configA = this.getProperty(beanC_configC, "configA");
			Assertions.assertNotNull(beanC_configC_configA);
			Assertions.assertEquals(beanC_configA, beanC_configC_configA);
		}
		finally {
			moduleC.stop();
		}
	}
	
	@Test
	public void testNestedConfiguration_none_default() throws IOException, WinterCompilationException, IllegalArgumentException, IllegalAccessException, NoSuchFieldException, SecurityException {
		WinterModuleLoader moduleLoader = this.getWinterCompiler().compile(MODULEA, MODULED);
		WinterModuleProxy moduleD = moduleLoader.load(MODULED).build();
		moduleD.start();
		try {
			Object beanD = moduleD.getBean("beanD");
			Assertions.assertNotNull(beanD);
			
			Object beanD_configD = beanD.getClass().getField("configD").get(beanD);
			Assertions.assertNotNull(beanD_configD);
			
			Object beanD_configA = beanD.getClass().getField("configA").get(beanD);
			Assertions.assertNotNull(beanD_configA);
			
			Object beanD_configD_param1 = this.getProperty(beanD_configD, "param1");
			Assertions.assertNotNull(beanD_configD_param1);
			Assertions.assertEquals("abc", beanD_configD_param1);
			
			Object beanD_configD_configA = this.getProperty(beanD_configD, "configA");
			Assertions.assertNotNull(beanD_configD_configA);
			Assertions.assertEquals(beanD_configA, beanD_configD_configA);
			
			Object beanD_configA_param1 = this.getProperty(beanD_configA, "param1");
			Assertions.assertNotNull(beanD_configA_param1);
			Assertions.assertEquals("default param1", beanD_configA_param1);
			
			Object beanD_configA_param2 = this.getProperty(beanD_configA, "param2");
			Assertions.assertNotNull(beanD_configA_param2);
			Assertions.assertEquals(Integer.valueOf(1234), beanD_configA_param2);
		}
		finally {
			moduleD.stop();
		}
	}
	
	@Test
	public void testNestedConfiguration_builder_default() throws IOException, WinterCompilationException, IllegalArgumentException, IllegalAccessException, NoSuchFieldException, SecurityException {
		WinterModuleLoader moduleLoader = this.getWinterCompiler().compile(MODULEA, MODULED);
		
		Consumer<Object> configDConfigurator = builder -> {
			this.setProperty(builder, "param1", String.class, "def");
		};
		
		WinterModuleProxy moduleC = moduleLoader.load(MODULED).optionalDependency("configD", Consumer.class, configDConfigurator).build();
		moduleC.start();
		try {
			Object beanD = moduleC.getBean("beanD");
			Assertions.assertNotNull(beanD);
			
			Object beanD_configD = beanD.getClass().getField("configD").get(beanD);
			Assertions.assertNotNull(beanD_configD);
			
			Object beanD_configA = beanD.getClass().getField("configA").get(beanD);
			Assertions.assertNotNull(beanD_configA);
			
			Object beanD_configD_param1 = this.getProperty(beanD_configD, "param1");
			Assertions.assertNotNull(beanD_configD_param1);
			Assertions.assertEquals("def", beanD_configD_param1);
			
			Object beanD_configD_configA = this.getProperty(beanD_configD, "configA");
			Assertions.assertNotNull(beanD_configD_configA);
			Assertions.assertEquals(beanD_configA, beanD_configD_configA);
			
			Object beanD_configA_param1 = this.getProperty(beanD_configA, "param1");
			Assertions.assertNotNull(beanD_configA_param1);
			Assertions.assertEquals("default param1", beanD_configA_param1);
			
			Object beanD_configA_param2 = this.getProperty(beanD_configA, "param2");
			Assertions.assertNotNull(beanD_configA_param2);
			Assertions.assertEquals(Integer.valueOf(1234), beanD_configA_param2);
		}
		finally {
			moduleC.stop();
		}
	}
	
	@Test
	public void testNestedConfiguration_impl_default() throws IOException, WinterCompilationException, IllegalArgumentException, IllegalAccessException, NoSuchFieldException, SecurityException, ClassNotFoundException {
		WinterModuleLoader moduleLoader = this.getWinterCompiler().compile(MODULEA, MODULED);
		
		Class<?> configDClass = moduleLoader.loadClass(MODULED, "io.winterframework.test.config.moduleD.ConfigD");
		ConfigurationInvocationHandler configDHandler = new ConfigurationInvocationHandler(configDClass, Map.of("param1", "def"));
		Object configD = Proxy.newProxyInstance(configDClass.getClassLoader(),
                new Class<?>[] { configDClass },
                configDHandler);
		
		WinterModuleProxy moduleC = moduleLoader.load(MODULED).optionalDependency("configD", configDClass, configD).build();
		moduleC.start();
		try {
			Object beanD = moduleC.getBean("beanD");
			Assertions.assertNotNull(beanD);
			
			Object beanD_configD = beanD.getClass().getField("configD").get(beanD);
			Assertions.assertNotNull(beanD_configD);
			
			Object beanD_configA = beanD.getClass().getField("configA").get(beanD);
			Assertions.assertNotNull(beanD_configA);
			
			Object beanD_configD_param1 = this.getProperty(beanD_configD, "param1");
			Assertions.assertNotNull(beanD_configD_param1);
			Assertions.assertEquals("def", beanD_configD_param1);
			
			Object beanD_configD_configA = this.getProperty(beanD_configD, "configA");
			Assertions.assertNotNull(beanD_configD_configA);
			Assertions.assertEquals(beanD_configA, beanD_configD_configA);
			
			Object beanD_configA_param1 = this.getProperty(beanD_configA, "param1");
			Assertions.assertNotNull(beanD_configA_param1);
			Assertions.assertEquals("default param1", beanD_configA_param1);
			
			Object beanD_configA_param2 = this.getProperty(beanD_configA, "param2");
			Assertions.assertNotNull(beanD_configA_param2);
			Assertions.assertEquals(Integer.valueOf(1234), beanD_configA_param2);
		}
		finally {
			moduleC.stop();
		}
	}
}
