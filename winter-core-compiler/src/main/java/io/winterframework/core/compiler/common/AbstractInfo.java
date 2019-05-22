/**
 * 
 */
package io.winterframework.core.compiler.common;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;

import io.winterframework.core.compiler.spi.Info;
import io.winterframework.core.compiler.spi.QualifiedName;

/**
 * @author jkuhn
 *
 */
public abstract class AbstractInfo<T extends QualifiedName> extends ReporterInfo implements Info {

	protected ProcessingEnvironment processingEnvironment;

	protected T qname;
	
	public AbstractInfo(ProcessingEnvironment processingEnvironment, Element element, T qname) {
		this(processingEnvironment, element, null, qname);
	}
	
	public AbstractInfo(ProcessingEnvironment processingEnvironment, Element element, AnnotationMirror annotation, T qname) {
		super(processingEnvironment, element, annotation);
		this.qname = qname;
	}

	@Override
	public T getQualifiedName() {
		return this.qname;
	}
}
