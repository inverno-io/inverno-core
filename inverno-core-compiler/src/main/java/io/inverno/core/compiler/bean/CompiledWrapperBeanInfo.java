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
package io.inverno.core.compiler.bean;

import java.util.List;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.type.TypeMirror;

import io.inverno.core.annotation.Bean;
import io.inverno.core.compiler.spi.BeanQualifiedName;
import io.inverno.core.compiler.spi.ModuleBeanSocketInfo;
import io.inverno.core.compiler.spi.WrapperBeanInfo;

/**
 * <p>
 * Represents wrapper bean info. A wrapper bean is necessarily compiled because
 * a binary module only exposes module beans, the wrapper bean being hidden in
 * the module implementation.
 * </p>
 * 
 * @author <a href="mailto:jeremy.kuhn@inverno.io">Jeremy Kuhn</a>
 *
 */
class CompiledWrapperBeanInfo extends CommonModuleBeanInfo implements WrapperBeanInfo {

	private TypeMirror wrapperType;
	
	public CompiledWrapperBeanInfo(ProcessingEnvironment processingEnvironment, 
			Element element, 
			AnnotationMirror annotation, 
			BeanQualifiedName qname, 
			TypeMirror wrapperType, 
			TypeMirror type,
			TypeMirror providedType,
			List<? extends ModuleBeanSocketInfo> beanSocketInfos) {
		this(processingEnvironment, element, annotation, qname, wrapperType, type, providedType, Bean.Visibility.PUBLIC, Bean.Strategy.SINGLETON, null, null, beanSocketInfos);
	}
	
	public CompiledWrapperBeanInfo(
			ProcessingEnvironment processingEnvironment, 
			Element element, 
			AnnotationMirror annotation, 
			BeanQualifiedName qname, 
			TypeMirror wrapperType, 
			TypeMirror type,
			TypeMirror providedType,
			Bean.Visibility visibility, 
			Bean.Strategy strategy, 
			List<ExecutableElement> initElements, 
			List<ExecutableElement> destroyElements, 
			List<? extends ModuleBeanSocketInfo> beanSocketInfos) {
		super(processingEnvironment, element, annotation, qname, type, providedType, visibility, strategy, initElements, destroyElements, beanSocketInfos);
		
		this.wrapperType = wrapperType;
	}

	@Override
	public TypeMirror getWrapperType() {
		return this.wrapperType;
	}
}
