/*
 * Copyright 2024 Jeremy Kuhn
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

import io.inverno.core.annotation.Bean;
import io.inverno.core.compiler.spi.BeanQualifiedName;
import io.inverno.core.compiler.spi.ModuleBeanSocketInfo;
import io.inverno.core.compiler.spi.MutatorBeanInfo;
import io.inverno.core.compiler.spi.SocketBeanInfo;
import java.util.List;
import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.type.TypeMirror;

/**
 * <p>
 * 
 * </p>
 * 
 * @author <a href="jeremy.kuhn@inverno.io">Jeremy Kuhn</a>
 * @since 1.12
 */
public class CompiledMutatorBeanInfo extends CommonModuleBeanInfo implements MutatorBeanInfo {

	private final TypeMirror mutatorType;

	private final boolean required;
	
	private SocketBeanInfo mutatingSocket;

	public CompiledMutatorBeanInfo(ProcessingEnvironment processingEnvironment, Element element, AnnotationMirror annotation, BeanQualifiedName qname, TypeMirror mutatorType, TypeMirror type, TypeMirror providedType, Bean.Visibility visibility, Bean.Strategy strategy, List<ExecutableElement> initElements, List<ExecutableElement> destroyElements, List<? extends ModuleBeanSocketInfo> beanSocketInfos, boolean required) {
		super(processingEnvironment, element, annotation, qname, type, providedType, visibility, strategy, initElements, destroyElements, beanSocketInfos);
		this.mutatorType = mutatorType;
		this.required = required;
	}

	@Override
	public TypeMirror getMutatorType() {
		return this.mutatorType;
	}

	void setMutatingSocket(SocketBeanInfo mutatingSocket) {
		this.mutatingSocket = mutatingSocket;
	}
	
	@Override
	public SocketBeanInfo getMutatingSocket() {
		return this.mutatingSocket;
	}

	@Override
	public boolean isRequired() {
		return this.required;
	}
}
