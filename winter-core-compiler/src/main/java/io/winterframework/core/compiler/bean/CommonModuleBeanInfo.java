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

import java.util.Collections;
import java.util.List;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.type.TypeMirror;

import io.winterframework.core.annotation.Bean;
import io.winterframework.core.compiler.common.AbstractBeanInfo;
import io.winterframework.core.compiler.spi.BeanQualifiedName;
import io.winterframework.core.compiler.spi.ModuleBeanInfo;
import io.winterframework.core.compiler.spi.ModuleBeanSocketInfo;
import io.winterframework.core.compiler.spi.NestedBeanInfo;

/**
 * <p>
 * Represents a common bean info.
 * </p>
 * 
 * @author jkuhn
 * 
 */
class CommonModuleBeanInfo extends AbstractBeanInfo implements ModuleBeanInfo {

	private Bean.Visibility visibility;
	
	private Bean.Strategy strategy;
	
	private List<ExecutableElement> initElements;
	
	private List<ExecutableElement> destroyElements;

	private List<? extends NestedBeanInfo> nestedBeanInfos;
	
	private List<? extends ModuleBeanSocketInfo> socketInfos;
	
	private TypeMirror providedType;
	
	public CommonModuleBeanInfo(ProcessingEnvironment processingEnvironment, 
			Element element, 
			AnnotationMirror annotation, 
			BeanQualifiedName qname, 
			TypeMirror type, 
			TypeMirror providedType,
			List<? extends ModuleBeanSocketInfo> beanSocketInfos) {
		this(processingEnvironment, element, annotation, qname, type, providedType, Bean.Visibility.PUBLIC, Bean.Strategy.SINGLETON, null, null, beanSocketInfos);
	}
	
	public CommonModuleBeanInfo(ProcessingEnvironment processingEnvironment,
			Element element, 
			AnnotationMirror annotation, 
			BeanQualifiedName qname, 
			TypeMirror type, 
			TypeMirror providedType,
			Bean.Visibility visibility, 
			Bean.Strategy strategy, 
			List<ExecutableElement> initElements, 
			List<ExecutableElement> destroyElements, 
			List<? extends ModuleBeanSocketInfo> beanSocketInfos) {
		super(processingEnvironment, element, annotation, qname, type);
		
		this.providedType = providedType;
		this.visibility = visibility != null ? visibility : Bean.Visibility.PUBLIC;
		this.strategy = strategy != null ? strategy : Bean.Strategy.SINGLETON;
		this.initElements = initElements != null ? Collections.unmodifiableList(initElements) : Collections.emptyList();
		this.destroyElements = destroyElements != null ? Collections.unmodifiableList(destroyElements) : Collections.emptyList();
		this.nestedBeanInfos = Collections.emptyList();
		this.socketInfos = beanSocketInfos != null ? Collections.unmodifiableList(beanSocketInfos) : Collections.emptyList();
	}

	@Override
	public TypeMirror getProvidedType() {
		return this.providedType;
	}
	
	@Override
	public Bean.Strategy getStrategy() {
		return this.strategy;
	}

	@Override
	public Bean.Visibility getVisibility() {
		return this.visibility;
	}
	
	@Override
	public ExecutableElement[] getInitElements() {
		return this.initElements.stream().toArray(ExecutableElement[]::new);
	}

	@Override
	public ExecutableElement[] getDestroyElements() {
		return this.destroyElements.stream().toArray(ExecutableElement[]::new);
	}

	@Override
	public ModuleBeanSocketInfo[] getSockets() {
		return this.socketInfos.stream().toArray(ModuleBeanSocketInfo[]::new);
	}

	@Override
	public ModuleBeanSocketInfo[] getRequiredSockets() {
		return this.socketInfos.stream().filter(socketInfo -> !socketInfo.isOptional()).toArray(ModuleBeanSocketInfo[]::new);
	}

	@Override
	public ModuleBeanSocketInfo[] getOptionalSockets() {
		return this.socketInfos.stream().filter(socketInfo -> socketInfo.isOptional()).toArray(ModuleBeanSocketInfo[]::new);
	}
	
	void setNestedBeanInfos(List<? extends NestedBeanInfo> nestedBeanInfos) {
		this.nestedBeanInfos = nestedBeanInfos != null ? Collections.unmodifiableList(nestedBeanInfos) : Collections.emptyList();
	}
	
	@Override
	public NestedBeanInfo[] getNestedBeans() {
		return this.nestedBeanInfos.stream().toArray(NestedBeanInfo[]::new);
	}
}
