/**
 * 
 */
package io.winterframework.core.compiler.bean;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.AnnotationMirror;
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
	
	protected ExecutableElement socketElement;
	
	protected AnnotationMirror[] selectors;
	
	protected boolean optional;
	
	/**
	 * @param processingEnvironment
	 * @param element
	 * @param annotation
	 */
	public AbstractModuleBeanSocketInfo(ProcessingEnvironment processingEnvironment, 
			Element element, 
			BeanSocketQualifiedName qname, 
			TypeMirror type, 
			ExecutableElement socketElement, 
			AnnotationMirror[] selectors, 
			boolean optional) {
		super(processingEnvironment, element, qname);
		
		this.type = type;
		this.socketElement = socketElement;
		this.selectors = selectors != null ? selectors : new AnnotationMirror[0];
		this.optional = optional;
	}

	@Override
	public AnnotationMirror[] getSelectors() {
		return this.selectors;
	}
	
	@Override
	public TypeMirror getType() {
		return this.type;
	}

	@Override
	public ExecutableElement getSocketElement() {
		return this.socketElement;
	}

	@Override
	public boolean isOptional() {
		return this.optional;
	}
}
