/*
 * Copyright 2024 Jeremy Kuhn
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

import io.inverno.test.InvernoCompilationException;
import io.inverno.test.InvernoModuleLoader;
import io.inverno.test.InvernoModuleProxy;
import io.inverno.test.InvernoModuleProxyBuilder;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;
import javax.tools.Diagnostic;
import javax.tools.JavaFileObject;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * <p>
 * 
 * </p>
 * 
 * @author <a href="jeremy.kuhn@inverno.io">Jeremy Kuhn</a>
 * @since 1.12
 */
public class TestMutatorSocket extends AbstractCoreInvernoTest {

	private static final String MODULEA = "io.inverno.core.test.mutator.moduleA"; // required
	private static final String MODULEB = "io.inverno.core.test.mutator.moduleB"; // optional required
	private static final String MODULEC = "io.inverno.core.test.mutator.moduleC"; // unwired required
	private static final String MODULED = "io.inverno.core.test.mutator.moduleD"; // unwired !required
	private static final String MODULEE = "io.inverno.core.test.mutator.moduleE"; // optional !required
	
	@Test
	public void testRequiredMutatorSocket() throws IOException, InvernoCompilationException, IllegalArgumentException, IllegalAccessException, NoSuchFieldException, SecurityException, ClassNotFoundException, InstantiationException, InvocationTargetException, NoSuchMethodException {
		InvernoModuleLoader moduleLoader = this.getInvernoCompiler().compile(MODULEA);
		InvernoModuleProxyBuilder moduleProxyBuilder = moduleLoader.load(MODULEA);
		
		Runnable runnable = () -> {};
		
		InvernoModuleProxy moduleA = moduleProxyBuilder.dependencies(runnable).build();
		moduleA.start();
		try {
			Object beanA = moduleA.getBean("beanA");
			Assertions.assertNotNull(beanA);
			
			Object beanA_mutatedRunnable = beanA.getClass().getField("mutatedRunnable").get(beanA);
			Assertions.assertNotNull(beanA_mutatedRunnable);
			
			Assertions.assertEquals("io.inverno.core.test.mutator.moduleA.MutatedRunnable", beanA_mutatedRunnable.getClass().getCanonicalName());
			
			Object beanA_mutatedRunnable_runnable = beanA_mutatedRunnable.getClass().getField("runnable").get(beanA_mutatedRunnable);
			Assertions.assertNotNull(beanA_mutatedRunnable_runnable);
			
			Assertions.assertEquals(runnable, beanA_mutatedRunnable_runnable);
		}
		finally {
			moduleA.stop();
		}
	}
	
	@Test
	public void testNullRequiredMutatorSocket() throws IOException, InvernoCompilationException, IllegalArgumentException, IllegalAccessException, NoSuchFieldException, SecurityException, ClassNotFoundException, InstantiationException, InvocationTargetException, NoSuchMethodException {
		InvernoModuleLoader moduleLoader = this.getInvernoCompiler().compile(MODULEA);
		InvernoModuleProxyBuilder moduleProxyBuilder = moduleLoader.load(MODULEA);
		
		try {
			moduleProxyBuilder.dependencies((Runnable)null).build();
		}
		catch (Exception e) {
			Throwable current = e;
			while(current != null && !IllegalArgumentException.class.isAssignableFrom(current.getClass())) {
				current = current.getCause();
			}
			if(current == null) {
				Assertions.fail("We must have an IllegalArgumentException");
			}
			Assertions.assertEquals("Following non-optional sockets are null: runnableSocket", current.getMessage());
		}
	}
	
	@Test
	public void testOptionalMutatorSocket_required() throws IOException, InvernoCompilationException, IllegalArgumentException, IllegalAccessException, NoSuchFieldException, SecurityException, ClassNotFoundException, InstantiationException, InvocationTargetException, NoSuchMethodException {
		InvernoModuleLoader moduleLoader = this.getInvernoCompiler().compile(MODULEB);
		InvernoModuleProxyBuilder moduleProxyBuilder = moduleLoader.load(MODULEB);
		
		Runnable runnable = () -> {};
		
		InvernoModuleProxy moduleB = moduleProxyBuilder.dependencies(runnable).build();
		moduleB.start();
		try {
			Object beanA = moduleB.getBean("beanA");
			Assertions.assertNotNull(beanA);
			
			Object beanA_mutatedRunnable = beanA.getClass().getField("mutatedRunnable").get(beanA);
			Assertions.assertNotNull(beanA_mutatedRunnable);
			
			Assertions.assertEquals("io.inverno.core.test.mutator.moduleB.MutatedRunnable", beanA_mutatedRunnable.getClass().getCanonicalName());
			
			Object beanA_mutatedRunnable_runnable = beanA_mutatedRunnable.getClass().getField("runnable").get(beanA_mutatedRunnable);
			Assertions.assertNotNull(beanA_mutatedRunnable_runnable);
			
			Assertions.assertEquals(runnable, beanA_mutatedRunnable_runnable);
		}
		finally {
			moduleB.stop();
		}
	}

	@Test
	public void testOptionalMutatorSocket_notRequired() throws IOException, InvernoCompilationException, IllegalArgumentException, IllegalAccessException, NoSuchFieldException, SecurityException, ClassNotFoundException, InstantiationException, InvocationTargetException, NoSuchMethodException {
		InvernoModuleLoader moduleLoader = this.getInvernoCompiler().compile(MODULEE);
		InvernoModuleProxyBuilder moduleProxyBuilder = moduleLoader.load(MODULEE);

		Runnable runnable = () -> {};

		InvernoModuleProxy moduleE = moduleProxyBuilder.optionalDependency("runnableSocket", runnable).build();
		moduleE.start();
		try {
			Object beanA = moduleE.getBean("beanA");
			Assertions.assertNotNull(beanA);

			Object beanA_mutatedRunnable = beanA.getClass().getField("mutatedRunnable").get(beanA);
			Assertions.assertNotNull(beanA_mutatedRunnable);

			Assertions.assertEquals("io.inverno.core.test.mutator.moduleE.MutatedRunnable", beanA_mutatedRunnable.getClass().getCanonicalName());

			Object beanA_mutatedRunnable_runnable = beanA_mutatedRunnable.getClass().getField("runnable").get(beanA_mutatedRunnable);
			Assertions.assertNotNull(beanA_mutatedRunnable_runnable);

			Assertions.assertEquals(runnable, beanA_mutatedRunnable_runnable);
		}
		finally {
			moduleE.stop();
		}
	}

	@Test
	public void testNullOptionalMutatorSocket_notRequire() throws IOException, InvernoCompilationException, IllegalArgumentException, IllegalAccessException, NoSuchFieldException, SecurityException, ClassNotFoundException, InstantiationException, InvocationTargetException, NoSuchMethodException {
		InvernoModuleLoader moduleLoader = this.getInvernoCompiler().compile(MODULEE);
		InvernoModuleProxyBuilder moduleProxyBuilder = moduleLoader.load(MODULEE);

		InvernoModuleProxy moduleE = moduleProxyBuilder.build();
		moduleE.start();
		try {
			Object beanA = moduleE.getBean("beanA");
			Assertions.assertNotNull(beanA);

			Object beanA_mutatedRunnable = beanA.getClass().getField("mutatedRunnable").get(beanA);
			Assertions.assertNull(beanA_mutatedRunnable);
		}
		finally {
			moduleE.stop();
		}
	}
	
	@Test
	public void testUnwiredMutatorSocket_required() throws IOException, InvernoCompilationException, IllegalArgumentException, IllegalAccessException, NoSuchFieldException, SecurityException, ClassNotFoundException, InstantiationException, InvocationTargetException, NoSuchMethodException {
		InvernoModuleLoader moduleLoader = this.getInvernoCompiler().compile(MODULEC);
		
		List<Diagnostic<? extends JavaFileObject>> filteredDiagnostics = moduleLoader.getDiagnotics().stream()
			.filter(d -> d.getMessage(Locale.getDefault()).equals("Ignoring socket bean which is not wired"))
			.collect(Collectors.toList());
		
		Assertions.assertTrue(filteredDiagnostics.isEmpty());
	}

	@Test
	public void testUnwiredMutatorSocket_notRequired() throws IOException, InvernoCompilationException, IllegalArgumentException, IllegalAccessException, NoSuchFieldException, SecurityException, ClassNotFoundException, InstantiationException, InvocationTargetException, NoSuchMethodException {
		InvernoModuleLoader moduleLoader = this.getInvernoCompiler().compile(MODULED);

		List<Diagnostic<? extends JavaFileObject>> filteredDiagnostics = moduleLoader.getDiagnotics().stream()
			.filter(d -> d.getMessage(Locale.getDefault()).equals("Ignoring socket bean which is not wired"))
			.collect(Collectors.toList());

		Assertions.assertFalse(filteredDiagnostics.isEmpty());

		Diagnostic<? extends JavaFileObject> ignoringSocketBeanWarning = filteredDiagnostics.get(0);

		Assertions.assertTrue(ignoringSocketBeanWarning.getSource().getName().endsWith("RunnableSocket.java"));
	}
}
