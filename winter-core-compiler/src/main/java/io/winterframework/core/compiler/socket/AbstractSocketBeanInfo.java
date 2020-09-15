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
import java.util.Optional;
import java.util.Set;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.type.TypeMirror;

import io.winterframework.core.compiler.common.AbstractBeanInfo;
import io.winterframework.core.compiler.common.MutableSocketBeanInfo;
import io.winterframework.core.compiler.spi.BeanInfo;
import io.winterframework.core.compiler.spi.BeanQualifiedName;

/**
 * <p>
 * Base class for socket bean info.
 * </p>
 * 
 * @author jkuhn
 *
 */
abstract class AbstractSocketBeanInfo extends AbstractBeanInfo implements MutableSocketBeanInfo, WirableSocketBeanInfo {

	protected TypeMirror socketType;

	protected ExecutableElement socketElement;
	
	protected AnnotationMirror[] selectors;
	
	protected boolean optional;
	
	protected Set<BeanQualifiedName> wiredBeans;
	
	protected boolean wired;
	
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
	public Optional<ExecutableElement> getSocketElement() {
		return Optional.ofNullable(this.socketElement);
	}

	@Override
	public boolean isOptional() {
		return this.optional;
	}
	
	@Override
	public void setOptional(boolean optional) {
		this.optional = optional;
	}

	@Override
	public void setWiredBeans(Set<BeanQualifiedName> wiredBeans) {
		this.wiredBeans = wiredBeans != null ? Collections.unmodifiableSet(wiredBeans) : Collections.emptySet();
	}
	
	@Override
	public BeanQualifiedName[] getWiredBeans() {
		return this.wiredBeans.stream().toArray(BeanQualifiedName[]::new);
	}

	@Override
	public void setWired(boolean wired) {
		this.wired = wired;
	}
	
	@Override
	public boolean isWired() {
		return this.wired;
	}
	
	@Override
	public BeanInfo[] getNestedBeans() {
		// Regular socket bean do not provide nested beans
		return new BeanInfo[0];
	}
}
