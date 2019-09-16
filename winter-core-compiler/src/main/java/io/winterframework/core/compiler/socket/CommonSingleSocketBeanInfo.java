/**
 * 
 */
package io.winterframework.core.compiler.socket;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.ModuleElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;

import io.winterframework.core.compiler.common.MutableSingleSocketInfo;
import io.winterframework.core.compiler.spi.BeanInfo;
import io.winterframework.core.compiler.spi.BeanQualifiedName;
import io.winterframework.core.compiler.spi.SingleSocketBeanInfo;

/**
 * @author jkuhn
 *
 */
class CommonSingleSocketBeanInfo extends AbstractSocketBeanInfo implements SingleSocketBeanInfo, MutableSingleSocketInfo {

	private BeanInfo beanInfo;
	
	public CommonSingleSocketBeanInfo(ProcessingEnvironment processingEnvironment, 
			TypeElement element,
			AnnotationMirror annotation, 
			BeanQualifiedName qname, 
			TypeMirror type, 
			TypeMirror socketType,
			AnnotationMirror[] selectors,
			boolean optional) {
		super(processingEnvironment, element, annotation, qname, type, socketType, selectors, optional);
	}

	public CommonSingleSocketBeanInfo(ProcessingEnvironment processingEnvironment, 
			ModuleElement element,
			BeanQualifiedName qname,
			TypeMirror type, 
			TypeMirror socketType, 
			ExecutableElement socketElement,
			AnnotationMirror[] selectors,
			boolean optional) {
		super(processingEnvironment, element, qname, type, socketType, socketElement, selectors, optional);
	}

	/* (non-Javadoc)
	 * @see io.winterframework.core.compiler.spi.ModuleSocketInfo#isResolved()
	 */
	@Override
	public boolean isResolved() {
		return this.beanInfo != null;
	}

	@Override
	public void setBean(BeanInfo beanInfo) {
		this.beanInfo = beanInfo;
	}
	
	/* (non-Javadoc)
	 * @see io.winterframework.core.compiler.spi.ModuleSingleSocketInfo#getBean()
	 */
	@Override
	public BeanInfo getBean() {
		return this.beanInfo;
	}

}
