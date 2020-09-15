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
package io.winterframework.core.compiler.socket;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.ModuleElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;

import io.winterframework.core.compiler.spi.BeanQualifiedName;
import io.winterframework.core.compiler.spi.ConfigurationSocketBeanInfo;

/**
 * @author jkuhn
 *
 */
public class BinaryConfigurationSocketBeanInfo extends CommonSingleSocketBeanInfo implements ConfigurationSocketBeanInfo {

	public BinaryConfigurationSocketBeanInfo(ProcessingEnvironment processingEnvironment, 
			ModuleElement element,
			BeanQualifiedName qname, 
			TypeMirror type, 
			TypeMirror socketType, 
			ExecutableElement socketElement) {
		super(processingEnvironment, element, qname, type, socketType, socketElement, null, true);
	}

	public BinaryConfigurationSocketBeanInfo(ProcessingEnvironment processingEnvironment, 
			TypeElement element,
			AnnotationMirror annotation, 
			BeanQualifiedName qname, 
			TypeMirror type, 
			TypeMirror socketType) {
		super(processingEnvironment, element, annotation, qname, type, socketType, null, true);
	}
	
	@Override
	public boolean isWired() {
		// A binary socket is always wired
		return true;
	}
}
