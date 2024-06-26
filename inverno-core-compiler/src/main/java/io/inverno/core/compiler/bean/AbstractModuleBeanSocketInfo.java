/*
 * Copyright 2019 Jeremy KUHN
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

import java.util.Optional;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.type.TypeMirror;

import io.inverno.core.compiler.common.AbstractInfo;
import io.inverno.core.compiler.spi.BeanSocketQualifiedName;
import io.inverno.core.compiler.spi.ModuleBeanSocketInfo;

/**
 * <p>
 * Base class for {@link ModuleBeanSocketInfo} implementations.
 * </p>
 * 
 * @author <a href="mailto:jeremy.kuhn@inverno.io">Jeremy Kuhn</a>
 *
 */
abstract class AbstractModuleBeanSocketInfo extends AbstractInfo<BeanSocketQualifiedName> implements ModuleBeanSocketInfo {

	protected TypeMirror type;
	
	protected ExecutableElement socketElement;
	
	protected AnnotationMirror[] selectors;
	
	protected boolean optional;
	
	protected boolean lazy;

	public AbstractModuleBeanSocketInfo(ProcessingEnvironment processingEnvironment, 
			Element element, 
			BeanSocketQualifiedName qname, 
			TypeMirror type, 
			ExecutableElement socketElement, 
			AnnotationMirror[] selectors, 
			boolean optional,
			boolean lazy) {
		super(processingEnvironment, element, qname);
		this.type = type;
		this.socketElement = socketElement;
		this.selectors = selectors != null ? selectors : new AnnotationMirror[0];
		this.optional = optional;
		this.lazy = lazy;
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
	public Optional<ExecutableElement> getSocketElement() {
		// A bean socket should always have a socket element
		return Optional.ofNullable(this.socketElement);
	}

	@Override
	public boolean isOptional() {
		return this.optional;
	}
	
	@Override
	public boolean isLazy() {
		return this.lazy;
	}
}
