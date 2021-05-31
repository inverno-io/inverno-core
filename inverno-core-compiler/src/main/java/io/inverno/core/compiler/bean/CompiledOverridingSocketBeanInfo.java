/*
 * Copyright 2020 Jeremy KUHN
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

import java.util.Collections;
import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.type.TypeMirror;

import io.inverno.core.compiler.common.AbstractBeanInfo;
import io.inverno.core.compiler.common.MutableSingleSocketInfo;
import io.inverno.core.compiler.socket.WirableSocketBeanInfo;
import io.inverno.core.compiler.spi.BeanInfo;
import io.inverno.core.compiler.spi.BeanQualifiedName;
import io.inverno.core.compiler.spi.NestedBeanInfo;
import io.inverno.core.compiler.spi.OverridingSocketBeanInfo;

/**
 * @author <a href="mailto:jeremy.kuhn@inverno.io">Jeremy Kuhn</a>
 *
 */
public class CompiledOverridingSocketBeanInfo extends AbstractBeanInfo implements OverridingSocketBeanInfo, MutableSingleSocketInfo, WirableSocketBeanInfo {

	private TypeMirror socketType;
	
	private BeanInfo beanInfo;
	
	private Set<BeanQualifiedName> wiredBeans;
	
	public CompiledOverridingSocketBeanInfo(ProcessingEnvironment processingEnvironment, 
			Element element,
			AnnotationMirror annotation, 
			BeanQualifiedName qname, 
			TypeMirror type) {
		super(processingEnvironment, element, annotation, qname, type);
		
		this.socketType = processingEnvironment.getTypeUtils().getDeclaredType(processingEnvironment.getElementUtils().getTypeElement(Supplier.class.getCanonicalName()), type);
		this.wiredBeans = Collections.emptySet();
	}

	/*
	 * Does nothing since an overriding socket bean is always wired one overridable bean.
	 */
	@Override
	public void setWired(boolean wired) {
		
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
	public TypeMirror getSocketType() {
		return this.socketType;
	}

	@Override
	public NestedBeanInfo[] getNestedBeans() {
		return new NestedBeanInfo[0];
	}

	@Override
	public Optional<ExecutableElement> getSocketElement() {
		return Optional.empty();
	}

	@Override
	public AnnotationMirror[] getSelectors() {
		return new AnnotationMirror[0];
	}

	@Override
	public boolean isResolved() {
		return this.beanInfo != null;
	}

	@Override
	public BeanInfo getBean() {
		return this.beanInfo;
	}
	
	@Override
	public void setBean(BeanInfo bean) {
		this.beanInfo = bean;
	}
}
