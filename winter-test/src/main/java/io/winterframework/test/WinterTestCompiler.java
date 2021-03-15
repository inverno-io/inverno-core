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
package io.winterframework.test;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.tools.Diagnostic;
import javax.tools.JavaCompiler;
import javax.tools.JavaCompiler.CompilationTask;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.StandardLocation;
import javax.tools.ToolProvider;

/**
 * @author <a href="mailto:jeremy.kuhn@winterframework.io">Jeremy Kuhn</a>
 *
 */
public class WinterTestCompiler {
	
	private JavaCompiler compiler;
	private StandardJavaFileManager fileManager;
	
	private List<File> modulePaths;
	private List<File> annotationProcessorModulePaths;
	private List<File> classPaths;
	private File moduleSourcePath;
	private File moduleOutputPath;
	private File sourceOutputPath;
	
	private WinterCompilerDiagnosticListener diagnosticListener;
	
	public WinterTestCompiler(File moduleSourcePath, File sourceOutputPath, File moduleOutputPath, List<File> modulePaths, List<File> annotationProcessorModulePaths) throws IOException {
		this(moduleSourcePath, sourceOutputPath, moduleOutputPath, modulePaths, annotationProcessorModulePaths, List.of());
	}
	
	public WinterTestCompiler(File moduleSourcePath, File sourceOutputPath, File moduleOutputPath, List<File> modulePaths, List<File> annotationProcessorModulePaths, List<File> classPaths) throws IOException {
		this.diagnosticListener = new WinterCompilerDiagnosticListener();
		
		this.modulePaths = modulePaths;
		this.annotationProcessorModulePaths = annotationProcessorModulePaths;
		this.moduleSourcePath = moduleSourcePath;
		this.sourceOutputPath = sourceOutputPath;
		this.moduleOutputPath = moduleOutputPath;
		
		this.compiler = ToolProvider.getSystemJavaCompiler();
		
		this.fileManager = this.compiler.getStandardFileManager(null, null, null);

		this.fileManager.setLocation(StandardLocation.CLASS_PATH, this.classPaths);
		this.fileManager.setLocation(StandardLocation.MODULE_PATH, Stream.concat(this.modulePaths.stream(), this.annotationProcessorModulePaths.stream()).collect(Collectors.toList()));
//		this.fileManager.setLocation(StandardLocation.ANNOTATION_PROCESSOR_PATH, this.annotationProcessorModulePaths);
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
		
		return new WinterModuleLoader(Stream.concat(Stream.of(Paths.get(this.moduleOutputPath.toURI())), this.modulePaths.stream().map(File::toURI).map(Paths::get)).collect(Collectors.toList()), modulesWithLibs);
	}
	
	public List<Diagnostic<? extends JavaFileObject>> getDiagnostics() {
		return this.diagnosticListener.getDiagnotics();
	}
	
	public WinterTestCompiler withModulePaths(List<File> modulePaths) throws IOException {
		return new WinterTestCompiler(this.moduleSourcePath, this.sourceOutputPath, this.moduleOutputPath, Stream.concat(this.modulePaths.stream(), modulePaths.stream()).collect(Collectors.toList()), this.annotationProcessorModulePaths, this.classPaths);
	}

	public List<File> getModulePaths() {
		return modulePaths;
	}

	public List<File> getAnnotationProcessorModulePaths() {
		return annotationProcessorModulePaths;
	}

	public List<File> getClassPaths() {
		return classPaths;
	}

	public File getModuleSourcePath() {
		return moduleSourcePath;
	}

	public File getModuleOutputPath() {
		return moduleOutputPath;
	}

	public File getSourceOutputPath() {
		return sourceOutputPath;
	}
}