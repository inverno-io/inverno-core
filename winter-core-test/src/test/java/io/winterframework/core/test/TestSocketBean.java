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
package io.winterframework.core.test;

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;

import javax.sql.DataSource;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import com.sun.net.httpserver.HttpHandler;

import io.winterframework.test.WinterCompilationException;
import io.winterframework.test.WinterModuleLoader;
import io.winterframework.test.WinterModuleProxy;
import io.winterframework.test.WinterModuleProxyBuilder;

/**
 * 
 * @author <a href="mailto:jeremy.kuhn@winterframework.io">Jeremy Kuhn</a>
 *
 */
public class TestSocketBean extends AbstractCoreWinterTest {

	private static final String MODULEA = "io.winterframework.test.socketbean.moduleA";
	private static final String MODULED = "io.winterframework.test.socketbean.moduleD";
	private static final String MODULEE = "io.winterframework.test.socketbean.moduleE";
	private static final String MODULEF = "io.winterframework.test.socketbean.moduleF";
	private static final String MODULEH = "io.winterframework.test.socketbean.moduleH";

	private WinterModuleProxyBuilder moduleProxyBuilder;
	
	private DataSource extDataSource;
	private Runnable extRunnable;
	
	private Callable<String>[] extCallableArray;
	private List<Callable<String>> extCallableList;
	private Collection<Callable<String>> extCallableCollection;
	private Set<Callable<String>> extCallableSet;
	private List<Callable<String>> callables;
	
	private HttpHandler[] extHttpHandlerArray;
	private List<HttpHandler> extHttpHandlerList;
	private Collection<HttpHandler> extHttpHandlerCollection;
	private Set<HttpHandler> extHttpHandlerSet;
	private List<HttpHandler> httpHandlers;
	
	@SuppressWarnings("unchecked")
	public void init() throws IOException, WinterCompilationException {
		if(this.moduleProxyBuilder == null) {
			this.moduleProxyBuilder = this.getWinterCompiler().compile(MODULEA).load(MODULEA);
		}
		
		this.extDataSource = Mockito.mock(DataSource.class);
		this.extRunnable = Mockito.mock(Runnable.class);
		
		Callable<String> callable1 = Mockito.mock(Callable.class);
		Callable<String> callable2 = Mockito.mock(Callable.class);
		Callable<String> callable3 = Mockito.mock(Callable.class);
		Callable<String> callable4 = Mockito.mock(Callable.class);
		Callable<String> callable5 = Mockito.mock(Callable.class);
		Callable<String> callable6 = Mockito.mock(Callable.class);
		
		this.extCallableArray = new Callable[] {callable1, callable2};
		this.extCallableList = List.of(callable3);
		this.extCallableCollection = List.of(callable4);
		this.extCallableSet = Set.of(callable5, callable6);
		this.callables = List.of(callable1, callable2, callable3, callable4, callable5, callable6);
		
		HttpHandler handler1 = Mockito.mock(HttpHandler.class);
		HttpHandler handler2 = Mockito.mock(HttpHandler.class);
		HttpHandler handler3 = Mockito.mock(HttpHandler.class);
		HttpHandler handler4 = Mockito.mock(HttpHandler.class);
		HttpHandler handler5 = Mockito.mock(HttpHandler.class);
		HttpHandler handler6 = Mockito.mock(HttpHandler.class);
		
		this.extHttpHandlerArray = new HttpHandler[] {handler1, handler2};
		this.extHttpHandlerList = List.of(handler3);
		this.extHttpHandlerCollection = List.of(handler4);
		this.extHttpHandlerSet = Set.of(handler5, handler6);
		this.httpHandlers = List.of(handler1, handler2, handler3, handler4, handler5, handler6);
	}
	
	@SuppressWarnings("unchecked")
	@Test
	public void testSockets() throws IOException, WinterCompilationException, IllegalArgumentException, IllegalAccessException, NoSuchFieldException, SecurityException {
		this.init();
		
		WinterModuleProxy moduleProxy = this.moduleProxyBuilder
			.optionalDependency("extRunnable", this.extRunnable)
			.optionalDependency("extHttpHandlerArray", this.extHttpHandlerArray)
			.optionalDependency("extHttpHandlerList", this.extHttpHandlerList)
			.optionalDependency("extHttpHandlerCollection", this.extHttpHandlerCollection)
			.optionalDependency("extHttpHandlerSet", this.extHttpHandlerSet)
			.dependencies(this.extCallableCollection, this.extCallableArray, this.extDataSource, this.extCallableSet, this.extCallableList).build();
		
		//public static Builder with(Collection<Callable<String>> extCallableCollection, Callable<String>[] extCallableArray, DataSource extDataSource, Set<Callable<String>> extCallableSet, List<Callable<String>> extCallableList) {
		
		moduleProxy.start();
		
		try {
			Object beanCallable = moduleProxy.getBean("beanCallable");
			Assertions.assertNotNull(beanCallable);
			
			Object beanA = moduleProxy.getBean("beanA");
			Assertions.assertNotNull(beanA);
			
			Object beanA_dataSource = beanA.getClass().getField("dataSource").get(beanA);
			Assertions.assertEquals(this.extDataSource, beanA_dataSource);
			
			Object beanA_callables = beanA.getClass().getField("callables").get(beanA);
			Assertions.assertNotNull(beanA_callables);
			List<Callable<String>> beanA_callables_list = (List<Callable<String>>)beanA_callables;
			Assertions.assertEquals(this.callables.size() + 1, beanA_callables_list.size());
			Assertions.assertTrue(beanA_callables_list.containsAll(this.callables));
			Assertions.assertTrue(beanA_callables_list.contains(beanCallable));
			
			Object beanB = moduleProxy.getBean("beanB");
			Assertions.assertNotNull(beanB);
			
			Object beanB_runnable = beanB.getClass().getField("runnable").get(beanB);
			Assertions.assertEquals(this.extRunnable, beanB_runnable);
			
			Object beanB_handlers = beanB.getClass().getField("handlers").get(beanB);
			Assertions.assertNotNull(beanB_handlers);
			List<HttpHandler> beanB_handlers_list = (List<HttpHandler>)beanB_handlers;
			Assertions.assertEquals(this.httpHandlers.size(), beanB_handlers_list.size());
			Assertions.assertTrue(beanB_handlers_list.containsAll(this.httpHandlers));
		}
		finally {
			moduleProxy.stop();
		}
	}
	
	@Test
	public void testNullRequiredSocket() throws IOException, WinterCompilationException {
		this.init();
		
		try {
			this.moduleProxyBuilder
				.optionalDependency("extRunnable", this.extRunnable)
				.optionalDependency("extHttpHandlerArray", this.extHttpHandlerArray)
				.optionalDependency("extHttpHandlerList", this.extHttpHandlerList)
				.optionalDependency("extHttpHandlerCollection", this.extHttpHandlerCollection)
				.optionalDependency("extHttpHandlerSet", this.extHttpHandlerSet)
				.dependencies(this.extCallableCollection, this.extCallableArray, null, this.extCallableSet, this.extCallableList).build();
			Assertions.fail("Required socket can't be null");
		} catch (Exception e) {
			Throwable current = e;
			while(current != null && !IllegalArgumentException.class.isAssignableFrom(current.getClass())) {
				current = current.getCause();
			}
			if(current == null) {
				Assertions.fail("We must have an IllegalArgumentException");
			}
			Assertions.assertEquals("Following non-optional sockets are null: extDataSource", current.getMessage());
		}
	}
	
	@Test
	public void testNullOptionalSocket() throws IOException, WinterCompilationException, IllegalArgumentException, IllegalAccessException, NoSuchFieldException, SecurityException {
		this.init();
		
		WinterModuleProxy moduleProxy = this.moduleProxyBuilder
			.optionalDependency("extHttpHandlerArray", this.extHttpHandlerArray)
			.optionalDependency("extHttpHandlerList", this.extHttpHandlerList)
			.optionalDependency("extHttpHandlerCollection", this.extHttpHandlerCollection)
			.optionalDependency("extHttpHandlerSet", this.extHttpHandlerSet)
			.dependencies(this.extCallableCollection, this.extCallableArray, this.extDataSource, this.extCallableSet, this.extCallableList).build();
		
		try {
			moduleProxy.start();
		}
		catch (Throwable t) {
			t.printStackTrace();
		}
		try {
			Object beanA = moduleProxy.getBean("beanA");
			Assertions.assertNotNull(beanA);
			
			Object beanA_dataSource = beanA.getClass().getField("dataSource").get(beanA);
			Assertions.assertEquals(this.extDataSource, beanA_dataSource);
			
			Object beanB = moduleProxy.getBean("beanB");
			Assertions.assertNotNull(beanB);
			
			Object beanB_runnable = beanB.getClass().getField("runnable").get(beanB);
			Assertions.assertNull(beanB_runnable);
		}
		finally {
			moduleProxy.stop();
		}
	}
	
	@Test
	public void testExtendsSupplierError() throws IOException {
		try {
			this.getWinterCompiler().compile(MODULED);
			Assertions.fail("Should throw a WinterCompilationException");
		}
		catch(WinterCompilationException e) {
			Assertions.assertEquals(1, e.getDiagnostics().size());
			
			String beanModuleNameConflict = "A socket bean must extend java.util.function.Supplier";
			
			Assertions.assertTrue(e.getDiagnostics().stream().map(d -> d.getMessage(Locale.getDefault())).collect(Collectors.toList()).containsAll(List.of(beanModuleNameConflict)));
		}
	}
	
	@Test
	public void testPublicError() throws IOException {
		try {
			this.getWinterCompiler().compile(MODULEE);
			Assertions.fail("Should throw a WinterCompilationException");
		}
		catch(WinterCompilationException e) {
			Assertions.assertEquals(1, e.getDiagnostics().size());
			
			String beanModuleNameConflict = "A socket bean must be public";
			
			Assertions.assertTrue(e.getDiagnostics().stream().map(d -> d.getMessage(Locale.getDefault())).collect(Collectors.toList()).containsAll(List.of(beanModuleNameConflict)));
		}
	}
	
	@Test
	public void testInvalidNameError() throws IOException {
		try {
			this.getWinterCompiler().compile(MODULEF);
			Assertions.fail("Should throw a WinterCompilationException");
		}
		catch(WinterCompilationException e) {
			Assertions.assertEquals(1, e.getDiagnostics().size());
			
			String beanModuleNameConflict = "Invalid socket bean qualified name: QName part must be a valid Java identifier: # invalid 123";
			
			Assertions.assertTrue(e.getDiagnostics().stream().map(d -> d.getMessage(Locale.getDefault())).collect(Collectors.toList()).containsAll(List.of(beanModuleNameConflict)));
		}
	}
	
	@Test
	public void testIgnoreUnwired() throws IOException, WinterCompilationException {
		WinterModuleLoader moduleLoader = this.getWinterCompiler().compile(MODULEH);
		
		try {
			moduleLoader.load(MODULEH).optionalDependency("unwiredSocket", "").build();
			Assertions.fail("Should throw an IllegalArgumentException: unwired socket is ignored");
		} 
		catch (IllegalArgumentException e) {
			Assertions.assertEquals("No dependency unwiredSocket exists on module io.winterframework.test.socketbean.moduleH", e.getMessage());
		}
	}
}
