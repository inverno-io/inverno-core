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
import io.winterframework.core.compiler.spi.BeanQualifiedName;
import io.winterframework.core.compiler.spi.ModuleBeanSocketInfo;
import io.winterframework.core.compiler.spi.FactoryBeanInfo;

/**
 * <p>
 * Represents factory bean info. A factory bean is necessarily compiled because
 * a binary module only exposes module beans, the factory bean being hidden in
 * the module implementation.
 * </p>
 * 
 * @author jkuhn
 *
 */
class CompiledFactoryBeanInfo extends CommonModuleBeanInfo implements FactoryBeanInfo {

	private TypeMirror factoryType;
	
	public CompiledFactoryBeanInfo(ProcessingEnvironment processingEnvironment, 
			Element element, 
			AnnotationMirror annotation, 
			BeanQualifiedName qname, 
			TypeMirror factoryType, 
			TypeMirror type,
			List<? extends ModuleBeanSocketInfo> beanSocketInfos) {
		this(processingEnvironment, element, annotation, qname, factoryType, type, Bean.Visibility.PUBLIC, Bean.Strategy.SINGLETON, Collections.emptyList(), Collections.emptyList(), beanSocketInfos);
	}
	
	public CompiledFactoryBeanInfo(
			ProcessingEnvironment processingEnvironment, 
			Element element, 
			AnnotationMirror annotation, 
			BeanQualifiedName qname, 
			TypeMirror factoryType, 
			TypeMirror type,
			Bean.Visibility visibility, 
			Bean.Strategy strategy, 
			List<ExecutableElement> initElements, 
			List<ExecutableElement> destroyElements, 
			List<? extends ModuleBeanSocketInfo> beanSocketInfos) {
		super(processingEnvironment, element, annotation, qname, type, null, visibility, strategy, initElements, destroyElements, beanSocketInfos);
		
		this.factoryType = factoryType;
	}

	@Override
	public TypeMirror getFactoryType() {
		return this.factoryType;
	}
}
