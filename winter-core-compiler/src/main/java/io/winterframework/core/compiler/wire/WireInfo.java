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

import io.winterframework.core.compiler.common.GenericReporterInfo;
import io.winterframework.core.compiler.spi.BeanQualifiedName;
import io.winterframework.core.compiler.spi.QualifiedName;
import io.winterframework.core.compiler.spi.QualifiedNameFormatException;

/**
 * <p>Base class for wire info associating one or more beans to a socket.</p>
 * 
 * @author <a href="mailto:jeremy.kuhn@winterframework.io">Jeremy Kuhn</a>
 *
 */
public abstract class WireInfo<T extends QualifiedName> extends GenericReporterInfo {

	private BeanQualifiedName[] beanQNames;
	
	private T socketQName;
	
	public WireInfo(ProcessingEnvironment processingEnvironment, ModuleElement element, AnnotationMirror annotation, BeanQualifiedName[] beanQNames, T socketQName) throws QualifiedNameFormatException {
		super(processingEnvironment, element, annotation);
		
		this.beanQNames = beanQNames;
		this.socketQName = socketQName;
	}

	public BeanQualifiedName[] getBeans() {
		return beanQNames;
	}

	public T getInto() {
		return socketQName;
	}
}
