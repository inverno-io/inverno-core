/**
 * 
 */
package io.winterframework.core.compiler.bean;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.ModuleElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeMirror;

import io.winterframework.core.compiler.common.MutableSingleSocketInfo;
import io.winterframework.core.compiler.spi.BeanInfo;
import io.winterframework.core.compiler.spi.BeanSocketQualifiedName;
import io.winterframework.core.compiler.spi.ModuleBeanSingleSocketInfo;

/**
 * @author jkuhn
 *
 */
class CommonModuleBeanSingleSocketInfo extends AbstractModuleBeanSocketInfo
		implements ModuleBeanSingleSocketInfo, MutableSingleSocketInfo {

	private BeanInfo beanInfo;
	
	/**
	 * @param processingEnvironment
	 * @param element
	 * @param qname
	 * @param type
	 * @param socketElement
	 * @param optional
	 */
	public CommonModuleBeanSingleSocketInfo(ProcessingEnvironment processingEnvironment, ModuleElement element, BeanSocketQualifiedName qname, TypeMirror type, ExecutableElement socketElement, boolean optional) {
		super(processingEnvironment, element, qname, type, socketElement, optional);
	}

	/**
	 * @param processingEnvironment
	 * @param element
	 * @param annotation
	 * @param qname
	 * @param type
	 * @param socketElement
	 * @param optional
	 */
	public CommonModuleBeanSingleSocketInfo(ProcessingEnvironment processingEnvironment, VariableElement element, BeanSocketQualifiedName qname, TypeMirror type, ExecutableElement socketElement, boolean optional) {
		super(processingEnvironment, element, qname, type, socketElement, optional);
	}

	/* (non-Javadoc)
	 * @see io.winterframework.core.compiler.spi.ModuleBeanSocketInfo#isResolved()
	 */
	@Override
	public boolean isResolved() {
		return this.beanInfo != null;
	}

	@Override
	public void setBean(BeanInfo bean) {
		this.beanInfo = bean;
	}
	
	/* (non-Javadoc)
	 * @see io.winterframework.core.compiler.spi.ModuleBeanSingleSocketInfo#getBean()
	 */
	@Override
	public BeanInfo getBean() {
		return this.beanInfo;
	}

}
