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

import io.winterframework.core.compiler.common.MutableMultiSocketInfo;
import io.winterframework.core.compiler.spi.BeanInfo;
import io.winterframework.core.compiler.spi.BeanQualifiedName;
import io.winterframework.core.compiler.spi.MultiSocketBeanInfo;
import io.winterframework.core.compiler.spi.MultiSocketType;

/**
 * @author jkuhn
 *
 */
class CommonMultiSocketBeanInfo extends AbstractSocketBeanInfo implements MultiSocketBeanInfo, MutableMultiSocketInfo {

	private MultiSocketType multiType;
	
	private BeanInfo[] beanInfos;

	public CommonMultiSocketBeanInfo(ProcessingEnvironment processingEnvironment, 
			ModuleElement element,
			BeanQualifiedName qname, 
			TypeMirror type, 
			TypeMirror socketType, 
			ExecutableElement socketElement,
			AnnotationMirror[] selectors,
			boolean optional, 
			MultiSocketType multiType) {
		super(processingEnvironment, element, qname, type, socketType, socketElement, selectors, optional);
		
		this.multiType = multiType;
	}
	
	public CommonMultiSocketBeanInfo(ProcessingEnvironment processingEnvironment, 
			TypeElement element,
			AnnotationMirror annotation, 
			BeanQualifiedName qname, 
			TypeMirror type, 
			TypeMirror socketType,
			AnnotationMirror[] selectors,
			boolean optional, 
			MultiSocketType multiType) {
		super(processingEnvironment, element, annotation, qname, type, socketType, selectors, optional);
		
		this.multiType = multiType;
	}

	@Override
	public boolean isResolved() {
		return this.beanInfos != null && this.beanInfos.length > 0;
	}
	
	@Override
	public void setBeans(BeanInfo[] beanInfos) {
		this.beanInfos = beanInfos;
	}
	
	@Override
	public BeanInfo[] getBeans() {
		return this.beanInfos;
	}

	@Override
	public MultiSocketType getMultiType() {
		return this.multiType;
	}
}
