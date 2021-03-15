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
package io.winterframework.core.compiler.common;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.type.TypeMirror;

import io.winterframework.core.compiler.spi.BeanInfo;
import io.winterframework.core.compiler.spi.BeanQualifiedName;

/**
 * <p>
 * Base class for Bean info: module bean, wrapper bean and socket bean.
 * </p>
 * 
 * @author <a href="mailto:jeremy.kuhn@winterframework.io">Jeremy Kuhn</a>
 *
 */
public abstract class AbstractBeanInfo extends AbstractInfo<BeanQualifiedName> implements BeanInfo {

	protected TypeMirror type;
	
	public AbstractBeanInfo(ProcessingEnvironment processingEnvironment, Element element, BeanQualifiedName qname, TypeMirror type) {
		this(processingEnvironment, element, null, qname, type);
	}

	public AbstractBeanInfo(ProcessingEnvironment processingEnvironment, Element element, AnnotationMirror annotation, BeanQualifiedName qname, TypeMirror type) {
		super(processingEnvironment, element, annotation, qname);
		
		this.qname = qname;
		this.type = type;
	}
	
	@Override
	public TypeMirror getType() {
		return this.type;
	}
}
