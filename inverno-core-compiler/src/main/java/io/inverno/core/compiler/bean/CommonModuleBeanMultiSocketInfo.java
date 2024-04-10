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
package io.inverno.core.compiler.bean;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.ModuleElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeMirror;

import io.inverno.core.compiler.common.MutableMultiSocketInfo;
import io.inverno.core.compiler.spi.BeanInfo;
import io.inverno.core.compiler.spi.BeanSocketQualifiedName;
import io.inverno.core.compiler.spi.ModuleBeanMultiSocketInfo;
import io.inverno.core.compiler.spi.MultiSocketType;

/**
 * <p>
 * Represents common multiple socket meta data.
 * </p>
 * 
 * @author <a href="mailto:jeremy.kuhn@inverno.io">Jeremy Kuhn</a>
 * 
 */
class CommonModuleBeanMultiSocketInfo extends AbstractModuleBeanSocketInfo implements ModuleBeanMultiSocketInfo, MutableMultiSocketInfo {

	private final MultiSocketType multiType;
	
	private BeanInfo[] beanInfos;
	
	public CommonModuleBeanMultiSocketInfo(ProcessingEnvironment processingEnvironment, 
			ModuleElement element, 
			BeanSocketQualifiedName qname, 
			TypeMirror type, 
			ExecutableElement socketElement,
			AnnotationMirror[] selectors,
			boolean optional, 
			boolean lazy, 
			MultiSocketType multiType) {
		super(processingEnvironment, element, qname, type, socketElement, selectors, optional, lazy);
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
			boolean lazy, 
			MultiSocketType multiType) {
		super(processingEnvironment, element, qname, type, socketElement, selectors, optional, lazy);
		this.multiType = multiType;
	}
	
	public CommonModuleBeanMultiSocketInfo(
			ProcessingEnvironment processingEnvironment, 
			ExecutableElement element, 
			BeanSocketQualifiedName qname, 
			TypeMirror type, 
			ExecutableElement socketElement,
			AnnotationMirror[] selectors,
			boolean optional, 
			boolean lazy, 
			MultiSocketType multiType) {
		super(processingEnvironment, element, qname, type, socketElement, selectors, optional, lazy);
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
