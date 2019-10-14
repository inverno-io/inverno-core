package io.winterframework.test;

import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import io.winterframework.core.test.AbstractWinterTest;
import io.winterframework.core.test.WinterCompilationException;

public class TestCompilationError extends AbstractWinterTest {

	private static final String MODULEA = "io.winterframework.test.error.moduleA";
	private static final String MODULEB = "io.winterframework.test.error.moduleB";
	private static final String MODULEC = "io.winterframework.test.error.moduleC";
	private static final String MODULED = "io.winterframework.test.error.moduleD";
	private static final String MODULEE = "io.winterframework.test.error.moduleE";
	private static final String MODULEF = "io.winterframework.test.error.moduleF";
	private static final String MODULEG = "io.winterframework.test.error.moduleG";
	
	@Test
	public void testBeanConcreteClass() throws IOException {
		try {
			this.getWinterCompiler().compile(MODULEA);
			Assertions.fail("Should throw a WinterCompilationException");
		}
		catch(WinterCompilationException e) {
			Assertions.assertEquals(2, e.getDiagnotics().size());
			
			String abstractBeanError = "A module bean or a wrapper bean must be a concrete class";
			String socketBeanSupplierError = "A socket bean element must extend java.util.function.Supplier";
			
			Assertions.assertTrue(e.getDiagnotics().stream().map(d -> d.getMessage(Locale.getDefault())).collect(Collectors.toList()).containsAll(List.of(abstractBeanError, socketBeanSupplierError)));
		}
	}
	
	@Test
	public void testInvalidBeanQualifiedName() throws IOException {
		try {
			this.getWinterCompiler().compile(MODULEB);
			Assertions.fail("Should throw a WinterCompilationException");
		}
		catch(WinterCompilationException e) {
			Assertions.assertEquals(1, e.getDiagnotics().size());
			
			String invalidBeanQualifiedNameError = "Invalid bean qualified name: QName part must be a valid Java identifier: #Invalid bean name";
			
			Assertions.assertTrue(e.getDiagnotics().stream().map(d -> d.getMessage(Locale.getDefault())).collect(Collectors.toList()).containsAll(List.of(invalidBeanQualifiedNameError)));
		}
	}
	
	@Test
	public void testPrivateConstructor() throws IOException {
		try {
			this.getWinterCompiler().compile(MODULEC);
			Assertions.fail("Should throw a WinterCompilationException");
		}
		catch(WinterCompilationException e) {
			Assertions.assertEquals(1, e.getDiagnotics().size());
			
			String privateConstructorError = "No public constructor defined in bean io.winterframework.test.error.moduleC:beanA";
			
			Assertions.assertTrue(e.getDiagnotics().stream().map(d -> d.getMessage(Locale.getDefault())).collect(Collectors.toList()).containsAll(List.of(privateConstructorError)));
		}
	}
	
	@Test
	public void testMultipleConstructors() throws IOException {
		try {
			this.getWinterCompiler().compile(MODULED);
			Assertions.fail("Should throw a WinterCompilationException");
		}
		catch(WinterCompilationException e) {
			Assertions.assertEquals(1, e.getDiagnotics().size());
			
			String multipleConstructorsError = "Multiple constructors are defined in module bean io.winterframework.test.error.moduleD:beanA, consider specifying a BeanSocket on the one to select";
			
			Assertions.assertTrue(e.getDiagnotics().stream().map(d -> d.getMessage(Locale.getDefault())).collect(Collectors.toList()).containsAll(List.of(multipleConstructorsError)));
		}
	}
	
	@Test
	public void testMultipleSocketConstructors() throws IOException {
		try {
			this.getWinterCompiler().compile(MODULEE);
			Assertions.fail("Should throw a WinterCompilationException");
		}
		catch(WinterCompilationException e) {
			Assertions.assertEquals(1, e.getDiagnotics().size());
			
			String multipleSocketConstructorsError = "Multiple constructors annotated with BeanSocket are defined in module bean io.winterframework.test.error.moduleE:beanA which is not permitted";
			
			Assertions.assertTrue(e.getDiagnotics().stream().map(d -> d.getMessage(Locale.getDefault())).collect(Collectors.toList()).containsAll(List.of(multipleSocketConstructorsError)));
		}
	}
	
	@Test
	public void testOptionalSocketNameConflict() throws IOException {
		try {
			this.getWinterCompiler().compile(MODULEF);
			Assertions.fail("Should throw a WinterCompilationException");
		}
		catch(WinterCompilationException e) {
			Assertions.assertEquals(1, e.getDiagnotics().size());
			
			String optionalSocketNameConflict = "Optional socket name is conflicting with a required socket name: runnable";
			
			Assertions.assertTrue(e.getDiagnotics().stream().map(d -> d.getMessage(Locale.getDefault())).collect(Collectors.toList()).containsAll(List.of(optionalSocketNameConflict)));
		}
	}
	
	@Test
	public void testModuleBeanNameConflict() throws IOException {
		try {
			this.getWinterCompiler().compile(MODULEG);
			Assertions.fail("Should throw a WinterCompilationException");
		}
		catch(WinterCompilationException e) {
			Set<String> messages = e.getDiagnotics().stream().map(d -> d.getMessage(Locale.getDefault())).collect(Collectors.toSet());
			
			Assertions.assertEquals(3, messages.size());
			
			String conflict1 = "Multiple beans with name beanD exist in module io.winterframework.test.error.moduleG";
			String conflict2 = "Multiple beans with name beanA exist in module io.winterframework.test.error.moduleG";
			String conflict3 = "Multiple beans with name beanC exist in module io.winterframework.test.error.moduleG";

			Assertions.assertTrue(messages.containsAll(Set.of(conflict1, conflict2, conflict3)));
		}
	}
	
	@Test
	public void testModuleBeanModuleConflict() throws IOException {
		// TODO Bean is conflicting with module: 
	}
}
