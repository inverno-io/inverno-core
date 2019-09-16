/**
 * 
 */
package io.winterframework.core.compiler.bean;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.ModuleElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeMirror;

import io.winterframework.core.compiler.common.MutableMultiSocketInfo;
import io.winterframework.core.compiler.spi.BeanInfo;
import io.winterframework.core.compiler.spi.BeanSocketQualifiedName;
import io.winterframework.core.compiler.spi.ModuleBeanMultiSocketInfo;
import io.winterframework.core.compiler.spi.MultiSocketType;

/**
 * @author jkuhn
 *
 */
class CommonModuleBeanMultiSocketInfo extends AbstractModuleBeanSocketInfo implements ModuleBeanMultiSocketInfo, MutableMultiSocketInfo {

	private BeanInfo[] beanInfos;
	
	private MultiSocketType multiType;
	
	public CommonModuleBeanMultiSocketInfo(ProcessingEnvironment processingEnvironment, 
			ModuleElement element, 
			BeanSocketQualifiedName qname, 
			TypeMirror type, 
			ExecutableElement socketElement,
			AnnotationMirror[] selectors,
			boolean optional, 
			MultiSocketType multiType) {
		super(processingEnvironment, element, qname, type, socketElement, selectors, optional);
		this.multiType = multiType;
	}
	
	public CommonModuleBeanMultiSocketInfo(
			ProcessingEnvironment processingEnvironment, 
			VariableElement element, 
			BeanSocketQualifiedName qname, 
			TypeMirror type, 
			ExecutableElement socketElement,
			AnnotationMirror[] selectors,
			boolean optional, 
			MultiSocketType multiType) {
		super(processingEnvironment, element, qname, type, socketElement, selectors, optional);
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
