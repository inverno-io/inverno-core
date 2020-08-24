/*
 * Copyright 2018 Jeremy KUHN
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

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

/**
 * @author jkuhn
 *
 */
public abstract class AbstractWinterTest {

	static {
		System.setProperty("org.apache.logging.log4j.simplelog.level", "DEBUG");
		System.setProperty("org.apache.logging.log4j.simplelog.logFile", "system.out");
	}
	
	private WinterCompiler winterCompiler;
	
	protected static final String WINTER_CORE = "../winter-core/target/classes";
	
	protected static final String WINTER_CORE_ANNOTATION = "../winter-core-annotation/target/classes";
	
	protected static final String WINTER_CORE_COMPILER = "../winter-core-compiler/target/classes";
	
	protected static final String WINTER_EXTERNAL_DEPENDENCIES = "../winter-core-test/target/dependency";
	
	protected static final String MODULE_SOURCE = "src/test/mods";

	protected static final String MODULE_SOURCE_TARGET = "target/generated-test-sources";
	
	protected static final String MODULE_TARGET = "target/test/mods";
	
	public AbstractWinterTest() {
		try {
			this.winterCompiler = new WinterCompiler(new File(WINTER_CORE), 
				new File(WINTER_CORE_ANNOTATION), 
				new File(WINTER_CORE_COMPILER),
				new File(WINTER_EXTERNAL_DEPENDENCIES),
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
