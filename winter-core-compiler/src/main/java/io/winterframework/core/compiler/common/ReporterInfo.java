/**
 * 
 */
package io.winterframework.core.compiler.common;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.tools.Diagnostic.Kind;

/**
 * @author jkuhn
 *
 */
public class ReporterInfo {

	protected ProcessingEnvironment processingEnvironment;

	private Element element;
	
	private AnnotationMirror annotation;
	
	private int errorCount = 0;
	
	private int warningCount = 0;
	
	public ReporterInfo(ProcessingEnvironment processingEnvironment, Element element) {
		this(processingEnvironment, element, null);
	}
	
	public ReporterInfo(ProcessingEnvironment processingEnvironment, Element element, AnnotationMirror annotation) {
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
