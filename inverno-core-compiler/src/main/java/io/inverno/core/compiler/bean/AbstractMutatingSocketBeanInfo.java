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

import io.inverno.core.compiler.common.AbstractSocketBeanInfo;
import io.inverno.core.compiler.spi.BeanQualifiedName;
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
public abstract class AbstractMutatingSocketBeanInfo extends AbstractSocketBeanInfo {

	private final boolean required;

	public AbstractMutatingSocketBeanInfo(ProcessingEnvironment processingEnvironment, Element element, AnnotationMirror annotation, BeanQualifiedName qname, TypeMirror type, TypeMirror socketType, AnnotationMirror[] selectors, boolean required, boolean optional) {
		super(processingEnvironment, element, annotation, qname, type, socketType, selectors, optional);
		this.required = required;
	}

	@Override
	public boolean isWired() {
		return this.required || super.isWired();
	}

	@Override
	public boolean isOptional() {
		return !this.required && super.isOptional();
	}
}
