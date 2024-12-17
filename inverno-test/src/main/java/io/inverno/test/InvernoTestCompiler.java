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
package io.inverno.test;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.UncheckedIOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
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
 * @author <a href="mailto:jeremy.kuhn@inverno.io">Jeremy Kuhn</a>
 */
public class InvernoTestCompiler {
	
	public static final Path DEFAULT_MODULE_SOURCE_PATH = Path.of("src/test/mods");
	public static final Path DEFAULT_GENERATED_SOURCE_PATH = Path.of("target/inverno-test-compiler/generated-test-sources");
	public static final Path DEFAULT_MODULE_TARGET_PATH = Path.of("target/inverno-test-compiler/mods");
	public static final Set<Path> DEFAULT_MODULE_PATH = Set.of(Path.of("target/inverno-test-compiler/dependencies"));
	
	private JavaCompiler compiler;
	private StandardJavaFileManager fileManager;
	
	private final Path moduleSourcePath;
	private final Path generatedSourcePath;
	private final Path moduleTargetPath;
	private final Set<Path> modulePaths;
	private final Set<Path> annotationProcessorModulePaths;
	private final Set<Path> classPaths;
	
	private final InvernoCompilerDiagnosticListener diagnosticListener;
	
	private InvernoTestCompiler(Path moduleSourcePath, Path sourceOutputPath, Path moduleOutputPath, Set<Path> modulePaths, Set<Path> annotationProcessorModulePaths) throws IOException {
		this(moduleSourcePath, sourceOutputPath, moduleOutputPath, modulePaths, annotationProcessorModulePaths, Set.of());
	}
	
	private InvernoTestCompiler(Path moduleSourcePath, Path sourceOutputPath, Path moduleOutputPath, Set<Path> modulePaths, Set<Path> annotationProcessorModulePaths, Set<Path> classPaths) throws IOException {
		this.diagnosticListener = new InvernoCompilerDiagnosticListener();
		
		this.moduleSourcePath = moduleSourcePath;
		this.generatedSourcePath = sourceOutputPath;
		this.moduleTargetPath = moduleOutputPath;
		this.modulePaths = modulePaths;
		this.annotationProcessorModulePaths = annotationProcessorModulePaths;
		this.classPaths = classPaths;
		
		this.compiler = ToolProvider.getSystemJavaCompiler();
		
		this.fileManager = this.compiler.getStandardFileManager(null, null, null);
		
		this.fileManager.setLocation(StandardLocation.CLASS_PATH, this.classPaths.stream().map(Path::toFile).collect(Collectors.toSet()));
		this.fileManager.setLocation(StandardLocation.MODULE_PATH, Stream.concat(this.modulePaths.stream(), this.annotationProcessorModulePaths.stream()).map(Path::toFile).collect(Collectors.toList()));
//		this.fileManager.setLocation(StandardLocation.ANNOTATION_PROCESSOR_PATH, this.annotationProcessorModulePaths);
		this.fileManager.setLocation(StandardLocation.MODULE_SOURCE_PATH, List.of(this.moduleSourcePath.toFile()));
		Files.createDirectories(this.generatedSourcePath);
		this.fileManager.setLocation(StandardLocation.SOURCE_OUTPUT, List.of(this.generatedSourcePath.toFile()));
		Files.createDirectories(this.moduleTargetPath);
		this.fileManager.setLocation(StandardLocation.CLASS_OUTPUT, List.of(this.moduleTargetPath.toFile()));
	}
	
	public InvernoModuleLoader compile(String... modules) throws IOException, InvernoCompilationException {
		CompilationTask task = this.compiler.getTask(new PrintWriter(System.out), this.fileManager, this.diagnosticListener, Arrays.asList("-Xlint:-options", "--module", Arrays.stream(modules).collect(Collectors.joining(",")), "-Ainverno.debug=true", "-Ainverno.verbose=true", "-Ainverno.generateDescriptor=true"), null, null);
		if(!task.call()) {
			for(Diagnostic<? extends JavaFileObject> d : this.diagnosticListener.getDiagnotics()) {
				System.err.println(d.toString());
			}
			throw new InvernoCompilationException(this.diagnosticListener.getDiagnotics());
		}
		Collection<String> modulesWithLibs = new HashSet<>();
		modulesWithLibs.addAll(Arrays.asList(modules));
		
		return new InvernoModuleLoader(Stream.concat(Stream.of(this.moduleTargetPath), this.modulePaths.stream()).collect(Collectors.toList()), modulesWithLibs, this.diagnosticListener.getDiagnotics());
	}
	
	public List<Diagnostic<? extends JavaFileObject>> getDiagnostics() {
		return this.diagnosticListener.getDiagnotics();
	}
	
	public InvernoTestCompiler withModulePaths(Set<Path> modulePaths) throws IOException {
		return new InvernoTestCompiler(this.moduleSourcePath, this.generatedSourcePath, this.moduleTargetPath, Stream.concat(this.modulePaths.stream(), modulePaths.stream()).collect(Collectors.toSet()), this.annotationProcessorModulePaths, this.classPaths);
	}

	public Set<Path> getModulePaths() {
		return modulePaths;
	}

	public Set<Path> getAnnotationProcessorModulePaths() {
		return annotationProcessorModulePaths;
	}

	public Set<Path> getClassPaths() {
		return classPaths;
	}

	public Path getModuleSourcePath() {
		return moduleSourcePath;
	}

	public Path getModuleOutputPath() {
		return moduleTargetPath;
	}

	public Path getGeneratedSourcePath() {
		return generatedSourcePath;
	}
	
	public void cleanModuleTarget() {
		this.deleteDir(this.generatedSourcePath);
		this.deleteDir(this.moduleTargetPath);
	}
	
	public void cleanModuleTarget(String... modules) {
		for(String module : modules) {
			this.deleteDir(this.generatedSourcePath.resolve(module));
			this.deleteDir(this.moduleTargetPath.resolve(module));
		}
	}
	
	private void deleteDir(Path path) {
		if(Files.exists(path)) {
			try {
				Files.walkFileTree(path, new SimpleFileVisitor<Path>() {

					@Override
					public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
						Files.delete(dir);
						return FileVisitResult.CONTINUE;
					}

					@Override
					public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
						Files.delete(file);
						return FileVisitResult.CONTINUE;
					}
				});
			}
			catch(IOException e) {
				throw new UncheckedIOException(e);
			}
		}
	}
	
	public static InvernoTestCompiler.Builder builder() {
		return new InvernoTestCompiler.Builder();
	}
	
	public static class Builder {
		
		private static final Function<Path, Path> DEFAULT_MODULE_OVERRIDE = Function.identity();
		private static final Function<Path, Path> DEFAULT_ANNOTATION_PROCESSOR_MODULE_OVERRIDE = path -> {
				if(path.getFileName().toString().startsWith("inverno-core-compiler")) {
					return path;
				}
				return null;
			};
		
		private Path moduleSourcePath = DEFAULT_MODULE_SOURCE_PATH;
		private Path generatedSourcePath = DEFAULT_GENERATED_SOURCE_PATH;
		private Path moduleTargetPath = DEFAULT_MODULE_TARGET_PATH;
		private Set<Path> modulePaths = DEFAULT_MODULE_PATH;
		private Function<Path, Path> moduleOverride = DEFAULT_MODULE_OVERRIDE;
		private Function<Path, Path> annotationProcessorModuleOverride = DEFAULT_ANNOTATION_PROCESSOR_MODULE_OVERRIDE;
		
		private Builder() {
			
		}
		
		public Builder moduleSourcePath(Path moduleSourcePath) {
			this.moduleSourcePath = moduleSourcePath != null ? moduleSourcePath : DEFAULT_MODULE_SOURCE_PATH;
			return this;
		}
		
		public Builder generatedSourcePath(Path generatedSourcePath) {
			this.generatedSourcePath = generatedSourcePath != null ? generatedSourcePath : DEFAULT_GENERATED_SOURCE_PATH;
			return this;
		}
		
		public Builder moduleTargetPath(Path moduleTargetPath) {
			this.moduleTargetPath = moduleTargetPath != null ? moduleTargetPath : DEFAULT_MODULE_TARGET_PATH;
			return this;
		}
		
		public Builder modulePaths(Set<Path> modulePaths) {
			this.modulePaths = modulePaths != null ? modulePaths : DEFAULT_MODULE_PATH;
			return this;
		}
		
		public Builder moduleOverride(Function<Path, Path> moduleOverride) {
			this.moduleOverride = moduleOverride != null ? moduleOverride : DEFAULT_MODULE_OVERRIDE;
			return this;
		}
		
		public Builder annotationProcessorModuleOverride(Function<Path, Path> moduleOverride) {
			this.annotationProcessorModuleOverride = annotationProcessorModuleOverride != null ? annotationProcessorModuleOverride : DEFAULT_ANNOTATION_PROCESSOR_MODULE_OVERRIDE;
			return this;
		}
		
		public InvernoTestCompiler build() {
			try {
				return new InvernoTestCompiler(
					this.moduleSourcePath, 
					this.generatedSourcePath, 
					this.moduleTargetPath, 
					this.filterModulePaths().map(this.moduleOverride).filter(Objects::nonNull).collect(Collectors.toSet()), 
					this.filterModulePaths().map(this.annotationProcessorModuleOverride).filter(Objects::nonNull).collect(Collectors.toSet())
				);
			}
			catch(IOException e) {
				throw new UncheckedIOException("Can't initialize Inverno Compiler", e);
			}
		}
		
		/**
		 * <p>
		 * Returns a stream of filtered module paths.
		 * </p>
		 * 
		 * <p>
		 * Specified module paths can be:
		 * </p>
		 * 
		 * <ul>
		 * <li>a module jar</li>
		 * <li>a directory containing a module descriptor</li>
		 * <li>a directory containing one of the above (we limit to one level and exclude any sub-directory that do not contain a module descriptor)</li>
		 * </ul>
		 * 
		 * @return a stream of module path
		 */
		private Stream<Path> filterModulePaths() {
			return this.modulePaths.stream().flatMap(path -> {
				if(Files.isDirectory(path)) {
					if(Files.exists(path.resolve("module-info.class"))) {
						return Stream.of(path);
					}
					else {
						try {
							return Files.list(path)
								.filter(subPath -> (Files.isDirectory(path) && Files.exists(subPath.resolve("module-info.class"))) || subPath.getFileName().toString().endsWith(".jar"));
						}
						catch(IOException e) {
							throw new UncheckedIOException(e);
						}
					}
				}
				else if(path.getFileName().toString().endsWith(".jar")) {
					return Stream.of(path);
				}
				return Stream.of();
			});
		}
	}
}