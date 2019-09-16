/**
 * 
 */
package io.winterframework.core.compiler.socket;

import java.util.Collections;
import java.util.Set;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.type.TypeMirror;

import io.winterframework.core.compiler.common.AbstractBeanInfo;
import io.winterframework.core.compiler.common.MutableModuleSocketInfo;
import io.winterframework.core.compiler.spi.BeanQualifiedName;

/**
 * @author jkuhn
 *
 */
abstract class AbstractSocketBeanInfo extends AbstractBeanInfo implements MutableModuleSocketInfo, WirableSocketBeanInfo {

	private TypeMirror socketType;

	private ExecutableElement socketElement;
	
	protected AnnotationMirror[] selectors;
	
	private boolean optional;
	
	private Set<BeanQualifiedName> wiredBeans;
	
	public AbstractSocketBeanInfo(ProcessingEnvironment processingEnvironment, 
			Element element,
			BeanQualifiedName qname, 
			TypeMirror type, 
			TypeMirror socketType, 
			ExecutableElement socketElement,
			AnnotationMirror[] selectors,
			boolean optional) {
		super(processingEnvironment, element, null, qname, type);
		
		this.socketType = socketType;
		this.socketElement = socketElement;
		this.selectors = selectors != null ? selectors : new AnnotationMirror[0];
		this.optional = optional;
		this.wiredBeans = Collections.emptySet();
	}
	
	public AbstractSocketBeanInfo(ProcessingEnvironment processingEnvironment, 
			Element element,
			AnnotationMirror annotation, 
			BeanQualifiedName qname, 
			TypeMirror type, 
			TypeMirror socketType, 
			AnnotationMirror[] selectors,
			boolean optional) {
		super(processingEnvironment, element, annotation, qname, type);
		
		this.socketType = socketType;
		this.selectors = selectors != null ? selectors : new AnnotationMirror[0];
		this.optional = optional;
		this.wiredBeans = Collections.emptySet();
	}
	
	@Override
	public AnnotationMirror[] getSelectors() {
		return this.selectors;
	}

	@Override
	public TypeMirror getSocketType() {
		return this.socketType;
	}

	@Override
	public ExecutableElement getSocketElement() {
		return this.socketElement;
	}

	@Override
	public boolean isOptional() {
		return this.optional;
	}
	
	@Override
	public void setOptional(boolean optional) {
		this.optional = optional;
	}

	public void setWiredBeans(Set<BeanQualifiedName> wiredBeans) {
		this.wiredBeans = wiredBeans != null ? Collections.unmodifiableSet(wiredBeans) : Collections.emptySet();
	}
	
	@Override
	public BeanQualifiedName[] getWiredBeans() {
		return this.wiredBeans.stream().toArray(BeanQualifiedName[]::new);
	}
}
