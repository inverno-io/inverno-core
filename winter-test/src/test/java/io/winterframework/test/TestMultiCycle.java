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
import io.winterframework.core.test.WinterModuleLoader;

/**
 * @author jkuhn
 *
 */
public class TestMultiCycle extends AbstractWinterTest {

	private static final String MODULEA = "io.winterframework.test.multicycle.moduleA";
	private static final String MODULEB = "io.winterframework.test.multicycle.moduleB";

	@Test
	public void testMultiCycle() throws IOException {
		try {
			this.getWinterCompiler().compile(MODULEA, MODULEB);
			Assertions.fail("Should throw a WinterCompilationException");
		}
		catch(WinterCompilationException e) {
			Assertions.assertEquals(4, e.getDiagnotics().size());
			
			String cycleMessage0 = "Module io.winterframework.test.multicycle.moduleB generated to file:///home/jkuhn/Devel/git/frmk/winter/winter-test/target/generated-test-sources/io.winterframework.test.multicycle.moduleB/io/winterframework/test/multicycle/moduleB/ModuleB.java";
			
			Assertions.assertEquals(cycleMessage0, e.getDiagnotics().get(0).getMessage(Locale.getDefault()));		
			
			String cycleMessage1 = "Bean io.winterframework.test.multicycle.moduleA:beanA forms a cycle in module io.winterframework.test.multicycle.moduleA\n" + 
					"  ┌────────────────────────────────┐\n" + 
					"  │                                │\n" + 
					"  │        io.winterframework.test.multicycle.moduleA:beanA\n" + 
					"  │                                │\n" + 
					"  │                                │ io.winterframework.test.multicycle.moduleA:beanA:callable\n" + 
					"  │                                │\n" + 
					"  │                                ▼\n" + 
					"  │        io.winterframework.test.multicycle.moduleB:beanA\n" + 
					"  ▲                                │\n" + 
					"  │                                │ io.winterframework.test.multicycle.moduleB:beanA:runnable\n" + 
					"  │                                │\n" + 
					"  │                                ▼\n" + 
					"  │    io.winterframework.test.multicycle.moduleB:runnableSocket\n" + 
					"  │                                ┊\n" + 
					"  │                                ┊ io.winterframework.test.multicycle.moduleB:runnableSocket\n" + 
					"  │                                ┊\n" + 
					"  └────────────────────────────────┘ "; 
			
			Assertions.assertEquals(cycleMessage1, e.getDiagnotics().get(1).getMessage(Locale.getDefault()));
			
			String cycleMessage2 = "Bean io.winterframework.test.multicycle.moduleB:beanA forms a cycle in module io.winterframework.test.multicycle.moduleA\n" + 
					"  ┌────────────────────────────────┐\n" + 
					"  │                                │\n" + 
					"  │        io.winterframework.test.multicycle.moduleA:beanA\n" + 
					"  │                                │\n" + 
					"  │                                │ io.winterframework.test.multicycle.moduleA:beanA:callable\n" + 
					"  │                                │\n" + 
					"  │                                ▼\n" + 
					"  │        io.winterframework.test.multicycle.moduleB:beanA\n" + 
					"  ▲                                │\n" + 
					"  │                                │ io.winterframework.test.multicycle.moduleB:beanA:runnable\n" + 
					"  │                                │\n" + 
					"  │                                ▼\n" + 
					"  │    io.winterframework.test.multicycle.moduleB:runnableSocket\n" + 
					"  │                                ┊\n" + 
					"  │                                ┊ io.winterframework.test.multicycle.moduleB:runnableSocket\n" + 
					"  │                                ┊\n" + 
					"  └────────────────────────────────┘ ";
			
			Assertions.assertEquals(cycleMessage2, e.getDiagnotics().get(2).getMessage(Locale.getDefault()));
			
			String cycleMessage3 = "Bean io.winterframework.test.multicycle.moduleB:runnableSocket forms a cycle in module io.winterframework.test.multicycle.moduleA\n" + 
					"  ┌────────────────────────────────┐\n" + 
					"  │                                │\n" + 
					"  │        io.winterframework.test.multicycle.moduleA:beanA\n" + 
					"  │                                │\n" + 
					"  │                                │ io.winterframework.test.multicycle.moduleA:beanA:callable\n" + 
					"  │                                │\n" + 
					"  │                                ▼\n" + 
					"  │        io.winterframework.test.multicycle.moduleB:beanA\n" + 
					"  ▲                                │\n" + 
					"  │                                │ io.winterframework.test.multicycle.moduleB:beanA:runnable\n" + 
					"  │                                │\n" + 
					"  │                                ▼\n" + 
					"  │    io.winterframework.test.multicycle.moduleB:runnableSocket\n" + 
					"  │                                ┊\n" + 
					"  │                                ┊ io.winterframework.test.multicycle.moduleB:runnableSocket\n" + 
					"  │                                ┊\n" + 
					"  └────────────────────────────────┘ "; 
			
			Assertions.assertEquals(cycleMessage3, e.getDiagnotics().get(3).getMessage(Locale.getDefault()));
		}
	}
}
