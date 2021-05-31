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
package io.inverno.core.compiler.bean;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.ExecutableType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeVariable;
import javax.tools.Diagnostic.Kind;

import io.inverno.core.annotation.NestedBean;
import io.inverno.core.compiler.spi.BeanInfo;
import io.inverno.core.compiler.spi.NestedBeanInfo;

/**
 * @author <a href="mailto:jeremy.kuhn@inverno.io">Jeremy Kuhn</a>
 *
 */
public class NestedBeanInfoFactory {

	private ProcessingEnvironment processingEnvironment;
	
	public NestedBeanInfoFactory(ProcessingEnvironment processingEnvironment) {
		this.processingEnvironment = processingEnvironment;
	}

	public List<? extends NestedBeanInfo> create(BeanInfo providingBean) {
		if(!(providingBean.getType() instanceof DeclaredType)) {
			return List.of();
		}
		
		DeclaredType type = (DeclaredType)providingBean.getType();
		
//		return type.asElement().getEnclosedElements().stream()
		return this.processingEnvironment.getElementUtils().getAllMembers((TypeElement)type.asElement()).stream()
			.filter(e -> e.getAnnotation(NestedBean.class) != null)
			.map(e -> (ExecutableElement)e)
			.filter(e -> {
				boolean valid = true;
				if(e.getParameters().size() > 0) {
					this.processingEnvironment.getMessager().printMessage(Kind.MANDATORY_WARNING, "Ignoring invalid " + NestedBean.class.getSimpleName() + " which should be defined as a no-argument method", e);
					valid = false;
				}
				if(e.getReturnType().getKind().equals(TypeKind.VOID)) {
					this.processingEnvironment.getMessager().printMessage(Kind.MANDATORY_WARNING, "Ignoring invalid " + NestedBean.class.getSimpleName() + " which should be defined as a non-void method", e);
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
					nestedBeanInfo = new CommonNestedBeanInfo(this.processingEnvironment, e, providingBean, nestedBeanType);
				}
				return nestedBeanInfo;
			})
			.filter(Objects::nonNull)
			.collect(Collectors.toList());
	}
}
