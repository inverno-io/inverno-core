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

import java.util.Optional;
import java.util.function.Supplier;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.ModuleElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;
import javax.tools.Diagnostic.Kind;

import io.inverno.core.annotation.Lazy;
import io.inverno.core.annotation.Selector;
import io.inverno.core.compiler.TypeErrorException;
import io.inverno.core.compiler.common.AbstractSocketInfoFactory;
import io.inverno.core.compiler.spi.BeanQualifiedName;
import io.inverno.core.compiler.spi.BeanSocketQualifiedName;
import io.inverno.core.compiler.spi.ModuleBeanSocketInfo;
import io.inverno.core.compiler.spi.MultiSocketType;
import io.inverno.core.compiler.spi.SocketBeanInfo;

/**
 * <p>
 * Factory used by {@link ModuleBeanInfoFactory} to create
 * {@link ModuleBeanSocketInfo}.
 * </p>
 * 
 * @author <a href="mailto:jeremy.kuhn@inverno.io">Jeremy Kuhn</a>
 *
 */
class ModuleBeanSocketInfoFactory extends AbstractSocketInfoFactory {

	private BeanQualifiedName beanQName;
	
	private TypeMirror supplierType;
	
	/**
	 * 
	 */
	protected ModuleBeanSocketInfoFactory(ProcessingEnvironment processingEnvironment, ModuleElement moduleElement, BeanQualifiedName beanQName) {
		super(processingEnvironment, moduleElement);
		
		this.beanQName = beanQName;
		this.supplierType = this.processingEnvironment.getTypeUtils().erasure(this.processingEnvironment.getElementUtils().getTypeElement(Supplier.class.getCanonicalName()).asType());
	}

	public static ModuleBeanSocketInfoFactory create(ProcessingEnvironment processingEnvironment, ModuleElement moduleElement, BeanQualifiedName beanQName) {
		return new ModuleBeanSocketInfoFactory(processingEnvironment, moduleElement, beanQName);
	}
	
	// Compiled
	public Optional<ModuleBeanSocketInfo> createBeanSocket(VariableElement variableElement) throws TypeErrorException {
		if(!variableElement.getKind().equals(ElementKind.PARAMETER)) {
			throw new IllegalArgumentException("Element must be a parameter");
		}
		ExecutableElement socketElement = (ExecutableElement)variableElement.getEnclosingElement();
		String socketName;
		AnnotationMirror[] selectors;
		boolean optional = false;
		if(socketElement.getKind().equals(ElementKind.CONSTRUCTOR)) {
			socketName = variableElement.getSimpleName().toString();
			selectors = variableElement.getAnnotationMirrors().stream().filter(a -> a.getAnnotationType().asElement().getAnnotation(Selector.class) != null).toArray(AnnotationMirror[]::new);
		}
		else {
			if(!socketElement.getModifiers().contains(Modifier.PUBLIC) || !socketElement.getSimpleName().toString().startsWith("set") || socketElement.getParameters().size() != 1) {
				this.processingEnvironment.getMessager().printMessage(Kind.MANDATORY_WARNING, "Invalid socket method which should be a single-argument setter method, socket will be ignored", socketElement);
				return Optional.empty();
			}
			
			selectors = socketElement.getAnnotationMirrors().stream().filter(a -> a.getAnnotationType().asElement().getAnnotation(Selector.class) != null).toArray(AnnotationMirror[]::new);
			
			socketName = socketElement.getSimpleName().toString().substring(3);
			socketName = Character.toLowerCase(socketName.charAt(0)) + socketName.substring(1);
			optional = true;
		}
		
		BeanSocketQualifiedName socketQName = new BeanSocketQualifiedName(this.beanQName, socketName);
		
		boolean lazy = variableElement.getAnnotation(Lazy.class) != null;
		TypeMirror socketType = variableElement.asType();
		MultiSocketType multiType = this.getMultiType(socketType);
		if(multiType != null) {
			socketType = this.getComponentType(socketType);
			if(lazy) {
				try {
					socketType = this.getLazyType(socketType);
				}
				catch(IllegalArgumentException e) {
					this.processingEnvironment.getMessager().printMessage(Kind.ERROR, e.getMessage(), variableElement);
					return Optional.empty();
				}
			}
			// Check if socket type can be resolved otherwise dependency injection might fail
			this.validateType(socketType);
			if(optional) {
				return Optional.of(new CommonModuleBeanMultiSocketInfo(this.processingEnvironment, socketElement, socketQName, socketType, socketElement, selectors, optional, lazy, multiType));
			}
			else {
				return Optional.of(new CommonModuleBeanMultiSocketInfo(this.processingEnvironment, variableElement, socketQName, socketType, socketElement, selectors, optional, lazy, multiType));
			}
		}
		else {
			if(lazy) {
				try {
					socketType = this.getLazyType(socketType);
				}
				catch(IllegalArgumentException e) {
					this.processingEnvironment.getMessager().printMessage(Kind.ERROR, e.getMessage(), variableElement);
					return Optional.empty();
				}
			}
			// Check if socket type can be resolved otherwise dependency injection might fail
			this.validateType(socketType);
			if(optional) {
				return Optional.of(new CommonModuleBeanSingleSocketInfo(this.processingEnvironment, socketElement, socketQName, socketType, socketElement, selectors, optional, lazy));
			}
			else {
				return Optional.of(new CommonModuleBeanSingleSocketInfo(this.processingEnvironment, variableElement, socketQName, socketType, socketElement, selectors, optional, lazy));
			}
		}
	}
	
	private TypeMirror getLazyType(TypeMirror type) throws IllegalArgumentException {
		if(!this.processingEnvironment.getTypeUtils().isSameType(this.processingEnvironment.getTypeUtils().erasure(type), this.supplierType)) {
			throw new IllegalArgumentException("Invalid lazy socket which should be of type " + Supplier.class.getCanonicalName());
		}

		if(((DeclaredType)type).getTypeArguments().isEmpty()) {
			return this.processingEnvironment.getElementUtils().getTypeElement(Object.class.getCanonicalName()).asType();
		}
		else {
			return ((DeclaredType)type).getTypeArguments().get(0);
		}
	}
	
	// Binary
	public ModuleBeanSocketInfo createBeanSocket(BeanQualifiedName beanQName, SocketBeanInfo moduleSocketInfo) {
		// TODO it would be better to use the actual beanQName but this broke cycle reporting 
//		BeanSocketQualifiedName socketQName = new BeanSocketQualifiedName(beanQName, moduleSocketInfo.getQualifiedName().getSimpleValue());
		BeanSocketQualifiedName socketQName = new BeanSocketQualifiedName(moduleSocketInfo.getQualifiedName(), moduleSocketInfo.getQualifiedName().getSimpleValue());
		return new CommonModuleBeanSingleSocketInfo(this.processingEnvironment, this.moduleElement, socketQName, moduleSocketInfo.getType(), null, null, moduleSocketInfo.isOptional(), false);
	}
}
