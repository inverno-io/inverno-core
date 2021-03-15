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
package io.winterframework.core.compiler.common;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;

import io.winterframework.core.compiler.spi.Info;
import io.winterframework.core.compiler.spi.QualifiedName;

/**
 * <p>
 * Base class for info.
 * </p>
 * 
 * @author <a href="mailto:jeremy.kuhn@winterframework.io">Jeremy Kuhn</a>
 *
 */
public abstract class AbstractInfo<T extends QualifiedName> extends GenericReporterInfo implements Info {

	protected T qname;
	
	public AbstractInfo(ProcessingEnvironment processingEnvironment, Element element, T qname) {
		this(processingEnvironment, element, null, qname);
	}
	
	public AbstractInfo(ProcessingEnvironment processingEnvironment, Element element, AnnotationMirror annotation, T qname) {
		super(processingEnvironment, element, annotation);
		this.qname = qname;
	}

	@Override
	public T getQualifiedName() {
		return this.qname;
	}
}
