package io.winterframework.test;

import java.io.IOException;
import java.util.Locale;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import io.winterframework.core.test.AbstractWinterTest;
import io.winterframework.core.test.WinterCompilationException;

public class TestConflict extends AbstractWinterTest {

	private static final String CONFLICT_MODULE = "io.winterframework.test.conflict";
	
	@Test
	public void testConflict() throws IOException {
		try {
			this.getWinterCompiler().compile(CONFLICT_MODULE);
			Assertions.fail("Should throw a WinterCompilationException");
		}
		catch(WinterCompilationException e) {
			Assertions.assertEquals(1, e.getDiagnotics().size());
			
			String conflictMessage = "Multiple beans matching socket io.winterframework.test.conflict:beanA:svc were found\n" + 
					"  - io.winterframework.test.conflict:serviceB of type io.winterframework.test.conflict.ServiceB\n" + 
					"  - io.winterframework.test.conflict:serviceA of type io.winterframework.test.conflict.ServiceA\n" + 
					"  \n" + 
					"  Consider specifying an explicit wiring in module io.winterframework.test.conflict (eg. @io.winterframework.core.annotation.Wire(beans=\"io.winterframework.test.conflict:serviceB\", into=\"io.winterframework.test.conflict:beanA:svc\") )\n" + 
					"   ";

			Assertions.assertEquals(conflictMessage, e.getDiagnotics().get(0).getMessage(Locale.getDefault()));
		}
	}
}
