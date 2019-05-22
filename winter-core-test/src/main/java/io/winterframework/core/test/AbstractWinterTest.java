/**
 * 
 */
package io.winterframework.core.test;

import java.io.File;
import java.io.IOException;

/**
 * @author jkuhn
 *
 */
public abstract class AbstractWinterTest {

	private WinterCompiler winterCompiler;
	
	protected static final String WINTER_CORE = "../winter-core/target/classes";
	
	protected static final String WINTER_CORE_ANNOTATION = "../winter-core-annotation/target/classes";
	
	protected static final String WINTER_CORE_COMPILER = "../winter-core-compiler/target/classes";
	
	protected static final String MODULE_SOURCE = "src/test/mods";

	protected static final String MODULE_SOURCE_TARGET = "target/generated-test-sources";
	
	protected static final String MODULE_TARGET = "target/test/mods";
	
	public AbstractWinterTest() {
		try {
			this.winterCompiler = new WinterCompiler(new File(WINTER_CORE), 
				new File(WINTER_CORE_ANNOTATION), 
				new File(WINTER_CORE_COMPILER), 
				new File(MODULE_SOURCE), 
				new File(MODULE_SOURCE_TARGET),
				new File(MODULE_TARGET));
		} catch (IOException e) {
			throw new RuntimeException("Can't initialize Winter Compiler", e);
		}
	}
	
	protected WinterCompiler getWinterCompiler() {
		return this.winterCompiler;
	}
}
