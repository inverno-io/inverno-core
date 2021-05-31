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
package io.inverno.core.compiler.common;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.tools.Diagnostic.Kind;

import io.inverno.core.compiler.spi.ReporterInfo;

/**
 * <p>
 * A reporter info is used to report and track info, warning and error on
 * module's elements and annotations during compilation.
 * </p>
 * 
 * @author <a href="mailto:jeremy.kuhn@inverno.io">Jeremy Kuhn</a>
 *
 */
public class GenericReporterInfo implements ReporterInfo {

	protected ProcessingEnvironment processingEnvironment;

	private Element element;
	
	private AnnotationMirror annotation;
	
	private int errorCount = 0;
	
	private int warningCount = 0;
	
	public GenericReporterInfo(ProcessingEnvironment processingEnvironment, Element element) {
		this(processingEnvironment, element, null);
	}
	
	public GenericReporterInfo(ProcessingEnvironment processingEnvironment, Element element, AnnotationMirror annotation) {
		this.processingEnvironment = processingEnvironment;
		this.element = element;
		this.annotation = annotation;
	}

	public Element getElement() {
		return element;
	}

	public AnnotationMirror getAnnotation() {
		return annotation;
	}
	
	public boolean hasError() {
		return this.errorCount > 0;
	}
	
	public boolean hasWarning() {
		return this.warningCount > 0;
	}
	
	public void error(String message) {
		if(this.element.getKind().equals(ElementKind.MODULE)) {
			// JDK bug
			this.processingEnvironment.getMessager().printMessage(Kind.ERROR, message, this.element);
		}
		else {
			this.processingEnvironment.getMessager().printMessage(Kind.ERROR, message, this.element, this.annotation);
		}
		this.errorCount++;
	}
	
	public void warning(String message) {
		if(this.element.getKind().equals(ElementKind.MODULE)) {
			// JDK bug
			this.processingEnvironment.getMessager().printMessage(Kind.MANDATORY_WARNING, message, this.element);
		}
		else {
			this.processingEnvironment.getMessager().printMessage(Kind.MANDATORY_WARNING, message, this.element, this.annotation);
		}
		this.warningCount++;
	}
}
