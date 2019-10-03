/**
 * 
 */
package io.winterframework.test;

import java.io.IOException;
import java.util.Locale;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import io.winterframework.core.test.AbstractWinterTest;
import io.winterframework.core.test.WinterCompilationException;

/**
 * @author jkuhn
 *
 */
public class TestCycle extends AbstractWinterTest {

	private static final String MODULE = "io.winterframework.test.cycle";

	@Test
	public void testCycle() throws IOException {
		try {
			this.getWinterCompiler().compile(MODULE);
			Assertions.fail("Should throw a WinterCompilationException");
		}
		catch(WinterCompilationException e) {
			Assertions.assertEquals(3, e.getDiagnotics().size());

			String cycleMessage1 = "Bean io.winterframework.test.cycle:beanA forms a cycle in module io.winterframework.test.cycle\n" + 
					"  ┌─────────────────────┐\n" + 
					"  │                     │\n" + 
					"  │    io.winterframework.test.cycle:beanA\n" + 
					"  │                     │\n" + 
					"  │                     │ io.winterframework.test.cycle:beanA:beanB\n" + 
					"  │                     │\n" + 
					"  │                     ▼\n" + 
					"  │    io.winterframework.test.cycle:beanB\n" + 
					"  ▲                     │\n" + 
					"  │                     │ io.winterframework.test.cycle:beanB:beanC\n" + 
					"  │                     │\n" + 
					"  │                     ▼\n" + 
					"  │    io.winterframework.test.cycle:beanC\n" + 
					"  │                     │\n" + 
					"  │                     │ io.winterframework.test.cycle:beanC:beanA\n" + 
					"  │                     │\n" + 
					"  └─────────────────────┘ "; 
			
			Assertions.assertEquals(cycleMessage1, e.getDiagnotics().get(0).getMessage(Locale.getDefault()));
			
			String cycleMessage2 = "Bean io.winterframework.test.cycle:beanB forms a cycle in module io.winterframework.test.cycle\n" + 
					"  ┌─────────────────────┐\n" + 
					"  │                     │\n" + 
					"  │    io.winterframework.test.cycle:beanA\n" + 
					"  │                     │\n" + 
					"  │                     │ io.winterframework.test.cycle:beanA:beanB\n" + 
					"  │                     │\n" + 
					"  │                     ▼\n" + 
					"  │    io.winterframework.test.cycle:beanB\n" + 
					"  ▲                     │\n" + 
					"  │                     │ io.winterframework.test.cycle:beanB:beanC\n" + 
					"  │                     │\n" + 
					"  │                     ▼\n" + 
					"  │    io.winterframework.test.cycle:beanC\n" + 
					"  │                     │\n" + 
					"  │                     │ io.winterframework.test.cycle:beanC:beanA\n" + 
					"  │                     │\n" + 
					"  └─────────────────────┘ ";
			
			Assertions.assertEquals(cycleMessage2, e.getDiagnotics().get(1).getMessage(Locale.getDefault()));
			
			String cycleMessage3 = "Bean io.winterframework.test.cycle:beanC forms a cycle in module io.winterframework.test.cycle\n" + 
					"  ┌─────────────────────┐\n" + 
					"  │                     │\n" + 
					"  │    io.winterframework.test.cycle:beanA\n" + 
					"  │                     │\n" + 
					"  │                     │ io.winterframework.test.cycle:beanA:beanB\n" + 
					"  │                     │\n" + 
					"  │                     ▼\n" + 
					"  │    io.winterframework.test.cycle:beanB\n" + 
					"  ▲                     │\n" + 
					"  │                     │ io.winterframework.test.cycle:beanB:beanC\n" + 
					"  │                     │\n" + 
					"  │                     ▼\n" + 
					"  │    io.winterframework.test.cycle:beanC\n" + 
					"  │                     │\n" + 
					"  │                     │ io.winterframework.test.cycle:beanC:beanA\n" + 
					"  │                     │\n" + 
					"  └─────────────────────┘ "; 
			
			Assertions.assertEquals(cycleMessage3, e.getDiagnotics().get(2).getMessage(Locale.getDefault()));
		}
	}
}
