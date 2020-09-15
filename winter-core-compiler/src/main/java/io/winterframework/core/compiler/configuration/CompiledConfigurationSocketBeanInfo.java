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
package io.winterframework.core.compiler.configuration;

import java.util.Collections;
import java.util.Optional;
import java.util.Set;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.type.TypeMirror;

import io.winterframework.core.compiler.common.AbstractBeanInfo;
import io.winterframework.core.compiler.common.MutableSingleSocketInfo;
import io.winterframework.core.compiler.common.MutableSocketBeanInfo;
import io.winterframework.core.compiler.socket.WirableSocketBeanInfo;
import io.winterframework.core.compiler.spi.BeanInfo;
import io.winterframework.core.compiler.spi.BeanQualifiedName;
import io.winterframework.core.compiler.spi.ConfigurationInfo;
import io.winterframework.core.compiler.spi.ConfigurationSocketBeanInfo;
import io.winterframework.core.compiler.spi.NestedConfigurationPropertyInfo;

/**
 * <p>
 * Represents the compiled {@link ConfigurationSocketBeanInfo} attached to a
 * {@link ConfigurationInfo} when a module is compiled.
 * </p>
 * 
 * @author jkuhn
 *
 */
public class CompiledConfigurationSocketBeanInfo extends AbstractBeanInfo implements ConfigurationSocketBeanInfo, MutableSingleSocketInfo, MutableSocketBeanInfo, WirableSocketBeanInfo { // Test for these

	private TypeMirror socketType;
	
	private BeanInfo beanInfo;
	
	private Set<BeanQualifiedName> wiredBeans;
	
	private boolean wired;
	
	private NestedConfigurationPropertyInfo[] nestedConfigurationProperties;
	
	public CompiledConfigurationSocketBeanInfo(ProcessingEnvironment processingEnvironment, 
			Element element,
			AnnotationMirror annotation, 
			BeanQualifiedName configurationQName, 
			TypeMirror type,
			NestedConfigurationPropertyInfo[] nestedConfigurationProperties) {
		super(processingEnvironment, element, annotation, new BeanQualifiedName(configurationQName.getModuleQName(), configurationQName.getBeanName()), type);
		
		this.nestedConfigurationProperties = nestedConfigurationProperties;
		this.socketType = this.processingEnvironment.getTypeUtils().getDeclaredType(this.processingEnvironment.getElementUtils().getTypeElement("io.winterframework.core.v1.Module.ConfigurationSocket"), this.type);
		this.wiredBeans = Collections.emptySet();
	}

	@Override
	public TypeMirror getSocketType() {
		return this.socketType;
	}
	
	@Override
	public BeanQualifiedName[] getWiredBeans() {
		return this.wiredBeans.stream().toArray(BeanQualifiedName[]::new);
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
	public boolean isOptional() {
		return true;
	}
	
	@Override
	public void setOptional(boolean optional) {
		// Does nothing a configuration socket is always optional
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

	@Override
	public void setWiredBeans(Set<BeanQualifiedName> wiredBeans) {
		this.wiredBeans = wiredBeans != null ? Collections.unmodifiableSet(wiredBeans) : Collections.emptySet();
	}
	
	@Override
	public BeanInfo[] getNestedBeans() {
		return this.nestedConfigurationProperties;
	}
	
	@Override
	public boolean isWired() {
		return this.wired;
	}
	
	@Override
	public void setWired(boolean wired) {
		this.wired = wired;
	}
}
