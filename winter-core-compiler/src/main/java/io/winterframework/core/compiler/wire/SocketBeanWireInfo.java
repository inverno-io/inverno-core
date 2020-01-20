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
package io.winterframework.core.compiler.wire;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.ModuleElement;

import io.winterframework.core.compiler.spi.BeanQualifiedName;
import io.winterframework.core.compiler.spi.QualifiedNameFormatException;

/**
 * <p>
 * Represents a socket bean wire info.
 * </p>
 * 
 * @author jkuhn
 *
 */
class SocketBeanWireInfo extends WireInfo<BeanQualifiedName> {

	public SocketBeanWireInfo(ProcessingEnvironment processingEnvironment, ModuleElement element,
			AnnotationMirror annotation, BeanQualifiedName[] beanQNames, BeanQualifiedName socketQName)
			throws QualifiedNameFormatException {
		super(processingEnvironment, element, annotation, beanQNames, socketQName);
	}

}
