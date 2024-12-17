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

import io.inverno.core.compiler.common.MutableSingleSocketInfo;
import io.inverno.core.compiler.spi.BeanInfo;
import io.inverno.core.compiler.spi.BeanQualifiedName;
import io.inverno.core.compiler.spi.MutatorBeanInfo;
import io.inverno.core.compiler.spi.NestedBeanInfo;
import io.inverno.core.compiler.spi.SingleSocketBeanInfo;
import java.util.Collections;
import java.util.List;
import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.type.TypeMirror;

/**
 * <p>
 * 
 * </p>
 * 
 * @author <a href="jeremy.kuhn@inverno.io">Jeremy Kuhn</a>
 * @since 1.6
 */
public class CompiledMutatingSingleSocketInfo extends AbstractMutatingSocketBeanInfo implements SingleSocketBeanInfo, MutableSingleSocketInfo {

	private BeanInfo beanInfo;
	
	private List<? extends NestedBeanInfo> nestedBeans;
	
	public CompiledMutatingSingleSocketInfo(ProcessingEnvironment processingEnvironment, Element element, AnnotationMirror annotation, BeanQualifiedName qname, TypeMirror type, TypeMirror socketType, AnnotationMirror[] selectors, boolean required, boolean optional) {
		super(processingEnvironment, element, annotation, qname, type, socketType, selectors, required, optional);
		this.nestedBeans = Collections.emptyList();
	}

	@Override
	public boolean isResolved() {
		return this.beanInfo != null;
	}

	@Override
	public void setBean(BeanInfo beanInfo) {
		this.beanInfo = beanInfo;
	}
	
	@Override
	public BeanInfo getBean() {
		return this.beanInfo;
	}

	void setNestedBeans(List<? extends NestedBeanInfo> nestedBeans) {
		this.nestedBeans = nestedBeans != null ? Collections.unmodifiableList(nestedBeans) : Collections.emptyList();
	}
	
	@Override
	public NestedBeanInfo[] getNestedBeans() {
		return this.nestedBeans.stream().toArray(NestedBeanInfo[]::new);
	}
}
