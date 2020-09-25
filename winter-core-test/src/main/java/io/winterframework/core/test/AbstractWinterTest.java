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
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

/**
 * @author jkuhn
 *
 */
public abstract class AbstractWinterTest {

	static {
		System.setProperty("org.apache.logging.log4j.simplelog.level", "DEBUG");
		System.setProperty("org.apache.logging.log4j.simplelog.logFile", "system.out");
	}
	
	private WinterTestCompiler winterCompiler;
	
	private static final String MODULE_SOURCE = "src/test/mods";

	private static final String MODULE_SOURCE_TARGET = "target/generated-test-sources";
	
	private static final String MODULE_TARGET = "target/test/mods";
	
	private static final String TEST_DEPENDENCIES = "target/dependency";
	
	protected AbstractWinterTest(Function<File, File> moduleOverride, Function<File, File> annotationProcessorModuleOverride) {
		moduleOverride = moduleOverride != null ? moduleOverride : file -> file;
		annotationProcessorModuleOverride = annotationProcessorModuleOverride != null ? annotationProcessorModuleOverride : file -> {
			if(file.getName().startsWith("winter-core-compiler")) {
				return file;
			}
			return null;
		};
		List<File> modulePaths = new LinkedList<>();
		List<File> annotationProcessorModulePath = new LinkedList<>();
		for(File dep : new File(TEST_DEPENDENCIES).listFiles()) {
			Optional.ofNullable(moduleOverride.apply(dep)).ifPresent(modulePaths::add);
			Optional.ofNullable(annotationProcessorModuleOverride.apply(dep)).ifPresent(annotationProcessorModulePath::add);
		}
		
		try {
			this.winterCompiler = new WinterTestCompiler(new File(MODULE_SOURCE), new File(MODULE_SOURCE_TARGET), new File(MODULE_TARGET), modulePaths, annotationProcessorModulePath);
		}
		catch (IOException e) {
			throw new RuntimeException("Can't initialize Winter Compiler", e);
		}
	}
	
	public AbstractWinterTest() {
		this(null, null);
	}
	
	protected WinterTestCompiler getWinterCompiler() {
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
