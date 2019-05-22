/**
 * 
 */
package io.winterframework.core.test;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.tools.Diagnostic;
import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.JavaCompiler.CompilationTask;
import javax.tools.StandardJavaFileManager;
import javax.tools.StandardLocation;
import javax.tools.ToolProvider;

/**
 * @author jkuhn
 *
 */
public class WinterCompiler {
	
	private JavaCompiler compiler;
	private StandardJavaFileManager fileManager;
	
	private File winterCorePath;
	private File winterCoreAnnotationPath;
	private File winterCoreCompilerPath;
	private File moduleSourcePath;
	private File moduleOutputPath;
	private File sourceOutputPath;
	
	private WinterCompilerDiagnosticListener diagnosticListener;
	
	public WinterCompiler(File winterCorePath, File winterCoreAnnotationPath, File winterCoreCompilerPath, File moduleSourcePath, File sourceOutputPath, File moduleOutputPath) throws IOException {
		this(winterCorePath, winterCoreAnnotationPath, winterCoreCompilerPath, moduleSourcePath, sourceOutputPath, moduleOutputPath, new File[0]);
	}
	
	public WinterCompiler(File winterCorePath, File winterCoreAnnotationPath, File winterCoreCompilerPath, File moduleSourcePath, File sourceOutputPath, File moduleOutputPath, File... extraModulePath) throws IOException {
		this.diagnosticListener = new WinterCompilerDiagnosticListener();
		
		this.winterCorePath = winterCorePath;
		this.winterCoreAnnotationPath = winterCoreAnnotationPath;
		this.winterCoreCompilerPath = winterCoreCompilerPath;
		this.moduleSourcePath = moduleSourcePath;
		this.moduleOutputPath = moduleOutputPath;
		this.sourceOutputPath = sourceOutputPath;
	
		this.compiler = ToolProvider.getSystemJavaCompiler();
		
		this.fileManager = this.compiler.getStandardFileManager(null, null, null);
		List<File> modulePaths = Stream.concat(Arrays.stream(extraModulePath), List.of(this.winterCorePath, this.winterCoreAnnotationPath, this.winterCoreCompilerPath).stream()).collect(Collectors.toList());
		if(extraModulePath != null && extraModulePath.length > 0) {
			modulePaths = Stream.concat(List.of(this.winterCorePath, this.winterCoreAnnotationPath, this.winterCoreCompilerPath).stream(), Arrays.stream(extraModulePath)).collect(Collectors.toList());
		}
		else {
			modulePaths = List.of(this.winterCorePath, this.winterCoreAnnotationPath, this.winterCoreCompilerPath);
		}
		this.fileManager.setLocation(StandardLocation.MODULE_PATH, modulePaths);
		this.fileManager.setLocation(StandardLocation.MODULE_SOURCE_PATH, List.of(this.moduleSourcePath));
		this.sourceOutputPath.mkdirs();
		this.fileManager.setLocation(StandardLocation.SOURCE_OUTPUT, List.of(this.sourceOutputPath));
		this.moduleOutputPath.mkdirs();
		this.fileManager.setLocation(StandardLocation.CLASS_OUTPUT, List.of(this.moduleOutputPath));
	}
	
	public WinterModuleLoader compile(String... modules) throws IOException, WinterCompilationException {
		CompilationTask task = this.compiler.getTask(new PrintWriter(System.out), this.fileManager, this.diagnosticListener, Arrays.asList("--module", Arrays.stream(modules).collect(Collectors.joining(","))), null, null);
		if(!task.call()) {
			for(Diagnostic<? extends JavaFileObject> d : this.diagnosticListener.getDiagnotics()) {
				System.err.println(d.toString());
			}
			throw new WinterCompilationException(this.diagnosticListener.getDiagnotics());
		}
				
		Collection<String> modulesWithLibs = new HashSet<>();
		modulesWithLibs.addAll(Arrays.asList(modules));
		
		Collection<Path> paths = new HashSet<>();
		paths.add(Paths.get(this.winterCorePath.toURI()));
		paths.add(Paths.get(this.winterCoreAnnotationPath.toURI()));
		paths.add(Paths.get(this.moduleOutputPath.toURI()));
		
		return new WinterModuleLoader(paths, modulesWithLibs);
	}
	
}
