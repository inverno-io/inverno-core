/**
 * 
 */
package io.winterframework.core.compiler.bean;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.type.TypeMirror;

import io.winterframework.core.compiler.common.AbstractInfo;
import io.winterframework.core.compiler.spi.BeanSocketQualifiedName;
import io.winterframework.core.compiler.spi.ModuleBeanSocketInfo;

/**
 * @author jkuhn
 *
 */
abstract class AbstractModuleBeanSocketInfo extends AbstractInfo<BeanSocketQualifiedName> implements ModuleBeanSocketInfo {

	protected TypeMirror type;
	
	protected  ExecutableElement socketElement;
	
	protected boolean optional;
	
	/**
	 * @param processingEnvironment
	 * @param element
	 * @param annotation
	 */
	public AbstractModuleBeanSocketInfo(ProcessingEnvironment processingEnvironment, Element element, BeanSocketQualifiedName qname, TypeMirror type, ExecutableElement socketElement, boolean optional) {
		super(processingEnvironment, element, qname);
		
		this.type = type;
		this.socketElement = socketElement;
		this.optional = optional;
	}

	/* (non-Javadoc)
	 * @see io.winterframework.core.compiler.spi.ModuleBeanSocketInfo#getType()
	 */
	@Override
	public TypeMirror getType() {
		return this.type;
	}

	/* (non-Javadoc)
	 * @see io.winterframework.core.compiler.spi.ModuleBeanSocketInfo#getSocketElement()
	 */
	@Override
	public ExecutableElement getSocketElement() {
		return this.socketElement;
	}

	/* (non-Javadoc)
	 * @see io.winterframework.core.compiler.spi.ModuleBeanSocketInfo#isOptional()
	 */
	@Override
	public boolean isOptional() {
		return this.optional;
	}
}
