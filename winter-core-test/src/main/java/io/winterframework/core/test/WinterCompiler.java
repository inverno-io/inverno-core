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
import java.io.PrintWriter;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

import javax.tools.Diagnostic;
import javax.tools.JavaCompiler;
import javax.tools.JavaCompiler.CompilationTask;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.StandardLocation;
import javax.tools.ToolProvider;

/**
 * @author jkuhn
 *
 */
public class WinterCompiler {
	
	private static final String CLASSPATH_EXTERNAL_DEPENDENCIES_REGEXP = "log4j-core-[1-9]\\d*\\.\\d+\\.\\d+\\.jar";
	
	private JavaCompiler compiler;
	private StandardJavaFileManager fileManager;
	
	private File winterCorePath;
	private File winterCoreAnnotationPath;
	private File winterCoreCompilerPath;
	private File[] winterExternalModulePaths;
	private File[] extraModulesPaths;
	private File moduleSourcePath;
	private File moduleOutputPath;
	private File sourceOutputPath;
	
	private WinterCompilerDiagnosticListener diagnosticListener;
	
	public WinterCompiler(File winterCorePath, File winterCoreAnnotationPath, File winterCoreCompilerPath, File winterExternalDependencies, File moduleSourcePath, File sourceOutputPath, File moduleOutputPath) throws IOException {
		this(winterCorePath, winterCoreAnnotationPath, winterCoreCompilerPath, winterExternalDependencies, moduleSourcePath, sourceOutputPath, moduleOutputPath, new File[0]);
	}
	
	public WinterCompiler(File winterCorePath, File winterCoreAnnotationPath, File winterCoreCompilerPath, File winterExternalDependencies, File moduleSourcePath, File sourceOutputPath, File moduleOutputPath, File... extraModulesPaths) throws IOException {
		this.diagnosticListener = new WinterCompilerDiagnosticListener();
		
		this.winterCorePath = winterCorePath;
		this.winterCoreAnnotationPath = winterCoreAnnotationPath;
		this.winterCoreCompilerPath = winterCoreCompilerPath;
		this.extraModulesPaths = extraModulesPaths;
		this.moduleSourcePath = moduleSourcePath;
		this.moduleOutputPath = moduleOutputPath;
		this.sourceOutputPath = sourceOutputPath;
	
		this.compiler = ToolProvider.getSystemJavaCompiler();
		
		this.fileManager = this.compiler.getStandardFileManager(null, null, null);
		List<File> classPaths = new ArrayList<>();
		List<File> modulePaths = new ArrayList<>();
		modulePaths.add(this.winterCorePath);
		modulePaths.add(this.winterCoreAnnotationPath);
		modulePaths.add(this.winterCoreCompilerPath);
		if(extraModulesPaths != null && extraModulesPaths.length > 0) {
			modulePaths.addAll(Arrays.asList(extraModulesPaths));
		}
		File[] winterExternalDependenciesPaths = winterExternalDependencies.listFiles();
		List<File> winterExternalModulePathsList = new ArrayList<>();
		if(winterExternalDependenciesPaths != null && winterExternalDependenciesPaths.length > 0) {
			for(File dependencyPath : winterExternalDependenciesPaths) {
				if(dependencyPath.getName().matches(CLASSPATH_EXTERNAL_DEPENDENCIES_REGEXP)) {
					classPaths.add(dependencyPath);
				}
				else {
					winterExternalModulePathsList.add(dependencyPath);
					modulePaths.add(dependencyPath);
				}
			}
		}
		this.winterExternalModulePaths = winterExternalModulePathsList.stream().toArray(File[]::new);
		this.fileManager.setLocation(StandardLocation.CLASS_PATH, classPaths);
		this.fileManager.setLocation(StandardLocation.MODULE_PATH, modulePaths);
		this.fileManager.setLocation(StandardLocation.MODULE_SOURCE_PATH, List.of(this.moduleSourcePath));
		this.sourceOutputPath.mkdirs();
		this.fileManager.setLocation(StandardLocation.SOURCE_OUTPUT, List.of(this.sourceOutputPath));
		this.moduleOutputPath.mkdirs();
		this.fileManager.setLocation(StandardLocation.CLASS_OUTPUT, List.of(this.moduleOutputPath));
	}
	
	public WinterModuleLoader compile(String... modules) throws IOException, WinterCompilationException {
		CompilationTask task = this.compiler.getTask(new PrintWriter(System.out), this.fileManager, this.diagnosticListener, Arrays.asList("--module", Arrays.stream(modules).collect(Collectors.joining(",")), "-Awinter.debug=true", "-Awinter.verbose=true", "-Awinter.generateDescriptor=true"), null, null);
		if(!task.call()) {
			for(Diagnostic<? extends JavaFileObject> d : this.diagnosticListener.getDiagnotics()) {
				System.err.println(d.toString());
			}
			throw new WinterCompilationException(this.diagnosticListener.getDiagnotics());
		}
				
		Collection<String> modulesWithLibs = new HashSet<>();
		modulesWithLibs.addAll(Arrays.asList(modules));
		
		Collection<Path> modulePaths = new HashSet<>();
		modulePaths.add(Paths.get(this.winterCorePath.toURI()));
		modulePaths.add(Paths.get(this.winterCoreAnnotationPath.toURI()));
		if(this.winterExternalModulePaths != null && this.winterExternalModulePaths.length > 0) {
			modulePaths.addAll(Arrays.stream(this.winterExternalModulePaths).map(File::toURI).map(Paths::get).collect(Collectors.toList()));
		}
		if(this.extraModulesPaths != null && this.extraModulesPaths.length > 0) {
			modulePaths.addAll(Arrays.stream(this.extraModulesPaths).map(File::toURI).map(Paths::get).collect(Collectors.toList()));
		}
		modulePaths.add(Paths.get(this.moduleOutputPath.toURI()));
		
		return new WinterModuleLoader(modulePaths, modulesWithLibs);
	}
	
	public List<Diagnostic<? extends JavaFileObject>> getDiagnostics() {
		return this.diagnosticListener.getDiagnotics();
	}
}
