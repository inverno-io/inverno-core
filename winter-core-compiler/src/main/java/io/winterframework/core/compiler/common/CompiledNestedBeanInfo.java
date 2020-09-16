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
package io.winterframework.core.compiler.common;

import java.util.Collections;
import java.util.List;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.ExecutableElement;

import io.winterframework.core.compiler.spi.BeanInfo;
import io.winterframework.core.compiler.spi.BeanQualifiedName;
import io.winterframework.core.compiler.spi.NestedBeanInfo;

/**
 * @author jkuhn
 *
 */
public class CompiledNestedBeanInfo extends AbstractBeanInfo implements NestedBeanInfo {

	private ExecutableElement accessorelement;
	
	private BeanInfo providingBean;
	
	private List<? extends NestedBeanInfo> nestedBeans;
	
	/**
	 * @param processingEnvironment
	 * @param element
	 * @param annotation
	 * @param qname
	 * @param type
	 */
	public CompiledNestedBeanInfo(ProcessingEnvironment processingEnvironment, ExecutableElement element, BeanInfo providingBean) {
		super(processingEnvironment, element, new BeanQualifiedName(providingBean.getQualifiedName().getModuleQName(), providingBean.getQualifiedName().getBeanName() + "." + element.getSimpleName().toString()), element.getReturnType());
		this.accessorelement = element;
		this.providingBean = providingBean;
		this.nestedBeans = Collections.emptyList();
	}

	public void setNestedBeans(List<? extends NestedBeanInfo> nestedBeans) {
		this.nestedBeans = nestedBeans != null ? Collections.unmodifiableList(nestedBeans) : Collections.emptyList();
	}
	
	@Override
	public NestedBeanInfo[] getNestedBeans() {
		return this.nestedBeans.stream().toArray(NestedBeanInfo[]::new);
	}

	@Override
	public String getName() {
		return this.accessorelement.getSimpleName().toString();
	}

	@Override
	public BeanInfo getProvidingBean() {
		return this.providingBean;
	}

	@Override
	public ExecutableElement getAccessorElement() {
		return this.accessorelement;
	}
}
