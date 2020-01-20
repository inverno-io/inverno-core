/*
 * Copyright 2018 Jeremy KUHN
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.winterframework.core.compiler.bean;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.ModuleElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeMirror;

import io.winterframework.core.compiler.common.MutableSingleSocketInfo;
import io.winterframework.core.compiler.spi.BeanInfo;
import io.winterframework.core.compiler.spi.BeanSocketQualifiedName;
import io.winterframework.core.compiler.spi.ModuleBeanSingleSocketInfo;

/**
 * <p>
 * Represents common single socket meta data.
 * </p>
 * 
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
	 * @param selectors
	 * @param optional
	 */
	public CommonModuleBeanSingleSocketInfo(ProcessingEnvironment processingEnvironment, 
			ModuleElement element, 
			BeanSocketQualifiedName qname, 
			TypeMirror type, 
			ExecutableElement socketElement,
			AnnotationMirror[] selectors,
			boolean optional) {
		super(processingEnvironment, element, qname, type, socketElement, selectors, optional);
	}

	/**
	 * @param processingEnvironment
	 * @param element
	 * @param qname
	 * @param type
	 * @param socketElement
	 * @param selectors
	 * @param optional
	 */
	public CommonModuleBeanSingleSocketInfo(ProcessingEnvironment processingEnvironment, 
			VariableElement element, 
			BeanSocketQualifiedName qname, 
			TypeMirror type, 
			ExecutableElement socketElement, 
			AnnotationMirror[] selectors,
			boolean optional) {
		super(processingEnvironment, element, qname, type, socketElement, selectors, optional);
	}

	@Override
	public boolean isResolved() {
		return this.beanInfo != null;
	}

	@Override
	public void setBean(BeanInfo bean) {
		this.beanInfo = bean;
	}
	
	@Override
	public BeanInfo getBean() {
		return this.beanInfo;
	}
}
