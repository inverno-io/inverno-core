/**
 * 
 */
package io.winterframework.core.compiler.wire;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.ModuleElement;

import io.winterframework.core.compiler.common.ReporterInfo;
import io.winterframework.core.compiler.spi.BeanQualifiedName;
import io.winterframework.core.compiler.spi.QualifiedName;
import io.winterframework.core.compiler.spi.QualifiedNameFormatException;

/**
 * @author jkuhn
 *
 */
public abstract class WireInfo<T extends QualifiedName> extends ReporterInfo {

	private BeanQualifiedName[] beanQNames;
	
	private T socketQName;
	
	/**
	 * 
	 */
	public WireInfo(ProcessingEnvironment processingEnvironment, ModuleElement element, AnnotationMirror annotation, BeanQualifiedName[] beanQNames, T socketQName) throws QualifiedNameFormatException {
		super(processingEnvironment, element, annotation);
		
		this.beanQNames = beanQNames;
		this.socketQName = socketQName;
	}

	public BeanQualifiedName[] getBeans() {
		return beanQNames;
	}

	public T getInto() {
		return socketQName;
	}
}
