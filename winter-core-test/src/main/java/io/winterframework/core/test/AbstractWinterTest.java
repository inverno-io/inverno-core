/**
 * 
 */
package io.winterframework.core.test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

/**
 * @author jkuhn
 *
 */
public abstract class AbstractWinterTest {

	static {
		System.setProperty("java.util.logging.SimpleFormatter.format", "%1$tF %1$tT %4$-7s %3$-50s %5$s %6$s%n");
	}

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
	
	protected void clearModuleTarget() {
		this.deleteDir(new File(MODULE_TARGET));
	}
	
	private void deleteDir(File file) {
	    File[] contents = file.listFiles();
	    if (contents != null) {
	        for (File f : contents) {
	            if (! Files.isSymbolicLink(f.toPath())) {
	                deleteDir(f);
	            }
	        }
	    }
	    file.delete();
	}
}
