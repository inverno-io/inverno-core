/**
 * 
 */
package io.winterframework.core.compiler.wire;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.ModuleElement;

import io.winterframework.core.compiler.spi.BeanQualifiedName;
import io.winterframework.core.compiler.spi.QualifiedNameFormatException;

/**
 * @author jkuhn
 *
 */
class SocketBeanWireInfo extends WireInfo<BeanQualifiedName> {

	public SocketBeanWireInfo(ProcessingEnvironment processingEnvironment, ModuleElement element,
			AnnotationMirror annotation, BeanQualifiedName[] beanQNames, BeanQualifiedName socketQName)
			throws QualifiedNameFormatException {
		super(processingEnvironment, element, annotation, beanQNames, socketQName);
	}

}
