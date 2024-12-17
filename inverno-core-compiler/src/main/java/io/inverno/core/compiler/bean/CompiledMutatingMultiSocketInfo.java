/*
 * Copyright 2024 Jeremy Kuhn
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

import io.inverno.core.compiler.common.MutableMultiSocketInfo;
import io.inverno.core.compiler.spi.BeanInfo;
import io.inverno.core.compiler.spi.BeanQualifiedName;
import io.inverno.core.compiler.spi.MultiSocketBeanInfo;
import io.inverno.core.compiler.spi.MultiSocketType;
import io.inverno.core.compiler.spi.NestedBeanInfo;
import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.type.TypeMirror;

/**
 * <p>
 * 
 * </p>
 * 
 * @author <a href="jeremy.kuhn@inverno.io">Jeremy Kuhn</a>
 * @since 1.6
 */
public class CompiledMutatingMultiSocketInfo extends AbstractMutatingSocketBeanInfo implements MultiSocketBeanInfo, MutableMultiSocketInfo {

	private final MultiSocketType multiType;
	
	private BeanInfo[] beanInfos;
	
	public CompiledMutatingMultiSocketInfo(ProcessingEnvironment processingEnvironment, Element element, AnnotationMirror annotation, BeanQualifiedName qname, TypeMirror type, TypeMirror socketType, AnnotationMirror[] selectors, boolean required, boolean optional, MultiSocketType multiType) {
		super(processingEnvironment, element, annotation, qname, type, socketType, selectors, required, optional);
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
	
	@Override
	public NestedBeanInfo[] getNestedBeans() {
		return new NestedBeanInfo[0];
	}
}
