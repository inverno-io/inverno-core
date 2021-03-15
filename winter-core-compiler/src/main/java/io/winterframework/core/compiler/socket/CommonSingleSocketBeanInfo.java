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
package io.winterframework.core.compiler.socket;

import java.util.Collections;
import java.util.List;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.ModuleElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;

import io.winterframework.core.compiler.common.MutableSingleSocketInfo;
import io.winterframework.core.compiler.spi.BeanInfo;
import io.winterframework.core.compiler.spi.BeanQualifiedName;
import io.winterframework.core.compiler.spi.NestedBeanInfo;
import io.winterframework.core.compiler.spi.SingleSocketBeanInfo;

/**
 * <p>
 * Represents a common single socket bean info.
 * </p>
 * 
 * @author <a href="mailto:jeremy.kuhn@winterframework.io">Jeremy Kuhn</a>
 *
 */
class CommonSingleSocketBeanInfo extends AbstractSocketBeanInfo implements SingleSocketBeanInfo, MutableSingleSocketInfo {

	private BeanInfo beanInfo;
	
	private List<? extends NestedBeanInfo> nestedBeans;
	
	public CommonSingleSocketBeanInfo(ProcessingEnvironment processingEnvironment, 
			TypeElement element,
			AnnotationMirror annotation, 
			BeanQualifiedName qname, 
			TypeMirror type, 
			TypeMirror socketType,
			AnnotationMirror[] selectors,
			boolean optional) {
		super(processingEnvironment, element, annotation, qname, type, socketType, selectors, optional);
		
		this.nestedBeans = Collections.emptyList();
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
		
		this.nestedBeans = Collections.emptyList();
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

	void setNestedBeans(List<? extends NestedBeanInfo> nestedBeans) {
		this.nestedBeans = nestedBeans != null ? Collections.unmodifiableList(nestedBeans) : Collections.emptyList();
	}
	
	@Override
	public NestedBeanInfo[] getNestedBeans() {
		return this.nestedBeans.stream().toArray(NestedBeanInfo[]::new);
	}
}
