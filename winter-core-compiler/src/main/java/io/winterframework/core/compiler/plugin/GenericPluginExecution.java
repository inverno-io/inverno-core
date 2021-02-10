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
import java.lang.annotation.Annotation;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import javax.tools.FileObject;
import javax.tools.JavaFileObject;
import javax.tools.StandardLocation;

import io.winterframework.core.compiler.common.GenericReporterInfo;
import io.winterframework.core.compiler.spi.BeanInfo;
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
	private Set<? extends Element> elements;
	private List<? extends BeanInfo> beans;
	
	private List<ReporterInfo> reporters;
	private List<JavaFileObject> generatedSourceFiles;
	
	private boolean failed;
	
	public GenericPluginExecution(ProcessingEnvironment processingEnvironment, ModuleQualifiedName module, Set<? extends Element> elements, List<? extends BeanInfo> beans) {
		this.processingEnvironment = processingEnvironment;
		this.module = module;
		this.elements = elements;
		this.beans = beans;
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
		// TODO I don't know what this check was for...
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

	@SuppressWarnings("unchecked")
	@Override
	public <T extends Element> Set<T> getElements() {
		return (Set<T>) this.elements;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public <T extends Element> Set<T> getElementsAnnotatedWith(Class<? extends Annotation> a) {
		return (Set<T>) this.elements.stream().filter(element -> element.getAnnotation(a) != null).collect(Collectors.toSet());
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public <T extends Element> Set<T> getElementsAnnotatedWith(TypeElement a) {
		if(a.getKind() != ElementKind.ANNOTATION_TYPE) {
			throw new IllegalArgumentException(a + " is not an annotation type");
		}
		TypeMirror annotationType = a.asType();
		return (Set<T>) this.elements.stream().filter(element -> element.getAnnotationMirrors().stream().anyMatch(anno -> this.processingEnvironment.getTypeUtils().isSameType(anno.getAnnotationType(), annotationType))).collect(Collectors.toSet());
	}
	
	@Override
	public BeanInfo[] getBeans() {
		return this.beans.stream().toArray(BeanInfo[]::new);
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
	public void createSourceFile(String name, Element[] originatingElements, Supplier<String> source) throws IOException {
		JavaFileObject sourceFile = this.processingEnvironment.getFiler().createSourceFile(name, originatingElements);
		try(Writer writer = sourceFile.openWriter()) {
			writer.write(source.get());
			writer.flush();
		}
		this.generatedSourceFiles.add(sourceFile);
	}
	
	@Override
	public void createResourceFile(String path, Element[] originatingElements, Supplier<String> resource) throws IOException {
		FileObject resourceFile = this.processingEnvironment.getFiler().createResource(StandardLocation.CLASS_OUTPUT, this.module.getValue() + "/", path, originatingElements);
		try(Writer writer = resourceFile.openWriter()) {
			writer.write(resource.get());
			writer.flush();
		}
	}
}
