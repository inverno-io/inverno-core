/*
 * Copyright 2020 Jeremy KUHN
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
package io.winterframework.core.compiler.plugin;

import java.io.IOException;
import java.io.Writer;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.tools.JavaFileObject;

import io.winterframework.core.compiler.common.GenericReporterInfo;
import io.winterframework.core.compiler.spi.ModuleQualifiedName;
import io.winterframework.core.compiler.spi.ReporterInfo;
import io.winterframework.core.compiler.spi.plugin.PluginExecution;

/**
 * @author jkuhn
 *
 */
class GenericPluginExecution implements PluginExecution {

	private ProcessingEnvironment processingEnvironment;
	private ModuleQualifiedName module; 
	private Set<TypeElement> elements;
	
	private List<ReporterInfo> reporters;
	private List<JavaFileObject> generatedSourceFiles;
	
	private boolean failed;
	
	public GenericPluginExecution(ProcessingEnvironment processingEnvironment, ModuleQualifiedName module, Set<TypeElement> elements) {
		this.processingEnvironment = processingEnvironment;
		this.module = module;
		this.elements = elements;
		this.reporters = new LinkedList<>();
		this.generatedSourceFiles = new LinkedList<>();
	}
	
	public void setFailed(boolean failed) {
		this.failed = failed;
	}
	
	public boolean hasFailed() {
		return this.failed;
	}

	public boolean hasError() {
		// TODO I don't know why this check was for...
		return /*this.reporters.size() == 0 || */this.reporters.stream().anyMatch(reporter -> reporter.hasError());
	}
	
	public boolean hasGeneratedSourceFiles() {
		return this.generatedSourceFiles.size() > 0;
	}
	
	public List<JavaFileObject> getGeneratedSourceFiles() {
		return this.generatedSourceFiles;
	}
	
	@Override
	public ModuleQualifiedName getModule() {
		return this.module;
	}

	@Override
	public Set<TypeElement> getElements() {
		return this.elements;
	}

	@Override
	public ReporterInfo getReporter(Element element) {
		ReporterInfo reporter = new GenericReporterInfo(this.processingEnvironment, element);
		this.reporters.add(reporter);
		return reporter;
	}

	@Override
	public ReporterInfo getReporter(Element element, AnnotationMirror annotation) {
		ReporterInfo reporter = new GenericReporterInfo(this.processingEnvironment, element, annotation);
		this.reporters.add(reporter);
		return reporter;
	}

	@Override
	public void createSourceFile(String name, TypeElement originatingElement, Supplier<String> source) throws IOException {
		JavaFileObject sourceFile = this.processingEnvironment.getFiler().createSourceFile(name, originatingElement);
		try(Writer writer = sourceFile.openWriter()) {
			writer.write(source.get());
			writer.flush();
		}
		this.generatedSourceFiles.add(sourceFile);
	}
}
