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
package io.winterframework.core.compiler.bean;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.ExecutableType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeVariable;

import io.winterframework.core.annotation.NestedBean;
import io.winterframework.core.compiler.common.AbstractBeanInfo;
import io.winterframework.core.compiler.spi.BeanInfo;
import io.winterframework.core.compiler.spi.BeanQualifiedName;
import io.winterframework.core.compiler.spi.ModuleBeanInfo;
import io.winterframework.core.compiler.spi.NestedBeanInfo;

/**
 * @author jkuhn
 *
 */
public class CommonNestedBeanInfo extends AbstractBeanInfo implements NestedBeanInfo {

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
	public CommonNestedBeanInfo(ProcessingEnvironment processingEnvironment, ExecutableElement element, BeanInfo providingBean, DeclaredType type) {
		super(processingEnvironment, element, new BeanQualifiedName(providingBean.getQualifiedName().getModuleQName(), providingBean.getQualifiedName().getBeanName() + "." + element.getSimpleName().toString()), type);
		this.accessorelement = element;
		this.providingBean = providingBean;
		
		this.nestedBeans = type.asElement().getEnclosedElements().stream()
			.filter(e -> e.getAnnotation(NestedBean.class) != null)
			.map(e -> (ExecutableElement)e)
			.filter(e -> {
				boolean valid = true;
				if(e.getParameters().size() > 0) {
					this.warning("Ignoring invalid " + NestedBean.class.getSimpleName() + " " + this.qname.getBeanName() + ", " + element + " should be a no-argument method");
					valid = false;
				}
				if(e.getReturnType().getKind().equals(TypeKind.VOID)) {
					this.warning("Ignoring invalid " + NestedBean.class.getSimpleName() + " " + this.qname.getBeanName() + ", " + element + " should be a non-void method");
					valid = false;
				}
				return valid;
			})
			.map(e -> {
				CommonNestedBeanInfo nestedBeanInfo = null;
				DeclaredType nestedBeanType = null;
				if(e.getReturnType() instanceof DeclaredType) {
					nestedBeanType = (DeclaredType)e.getReturnType();
				}
				else if(e.getReturnType() instanceof TypeVariable) {
					ExecutableType ras = (ExecutableType)this.processingEnvironment.getTypeUtils().asMemberOf(type, e);
					if(ras.getReturnType() instanceof DeclaredType) {
						nestedBeanType = (DeclaredType)ras.getReturnType();
					}
				}
				if(nestedBeanType != null) {
					nestedBeanInfo = new CommonNestedBeanInfo(this.processingEnvironment, e, this, nestedBeanType);
				}
				return nestedBeanInfo;
			})
			.filter(Objects::nonNull)
			.collect(Collectors.toList());
	}
	
	@Override
	public void error(String message) {
		if(this.providingBean instanceof ModuleBeanInfo) {
			// report on the annotated method in the module bean
			super.error(message);
		}
		else {
			// report on the root providing bean
			this.providingBean.error(message);			
		}
	}
	
	@Override
	public void warning(String message) {
		if(this.providingBean instanceof ModuleBeanInfo) {
			// report on the annotated method in the module bean
			super.warning(message);
		}
		else {
			// report on the root providing bean
			this.providingBean.warning(message);			
		}
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
