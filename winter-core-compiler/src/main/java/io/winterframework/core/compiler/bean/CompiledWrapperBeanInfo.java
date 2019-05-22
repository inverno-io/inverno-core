/**
 * 
 */
package io.winterframework.core.compiler.bean;

import java.util.Collections;
import java.util.List;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.type.TypeMirror;

import io.winterframework.core.annotation.Bean;
import io.winterframework.core.annotation.Scope;
import io.winterframework.core.compiler.spi.BeanQualifiedName;
import io.winterframework.core.compiler.spi.ModuleBeanSocketInfo;
import io.winterframework.core.compiler.spi.WrapperBeanInfo;

/**
 * @author jkuhn
 *
 */
class CompiledWrapperBeanInfo extends CommonModuleBeanInfo implements WrapperBeanInfo {

	private TypeMirror wrapperType;
	
	public CompiledWrapperBeanInfo(ProcessingEnvironment processingEnvironment, Element element, AnnotationMirror annotation, BeanQualifiedName qname, TypeMirror wrapperType, TypeMirror type, List<? extends ModuleBeanSocketInfo> beanSocketInfos) {
		this(processingEnvironment, element, annotation, qname, wrapperType, type, Bean.Visibility.PUBLIC, Scope.Type.SINGLETON, Collections.emptyList(), Collections.emptyList(), beanSocketInfos);
	}
	
	public CompiledWrapperBeanInfo(ProcessingEnvironment processingEnvironment, Element element, AnnotationMirror annotation, BeanQualifiedName qname, TypeMirror wrapperType, TypeMirror type, Bean.Visibility visibility, Scope.Type scope, List<ExecutableElement> initElements, List<ExecutableElement> destroyElements, List<? extends ModuleBeanSocketInfo> beanSocketInfos) {
		super(processingEnvironment, element, annotation, qname, type, visibility, scope, initElements, destroyElements, beanSocketInfos);
		
		this.wrapperType = wrapperType;
	}

	/* (non-Javadoc)
	 * @see io.winterframework.core.compiler.spi.ModuleWrapperBeanInfo#getWrapperType()
	 */
	@Override
	public TypeMirror getWrapperType() {
		return this.wrapperType;
	}

}
