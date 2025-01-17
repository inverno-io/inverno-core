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

import io.inverno.core.annotation.Lazy;
import io.inverno.core.annotation.Selector;
import io.inverno.core.compiler.TypeErrorException;
import io.inverno.core.compiler.common.AbstractSocketInfoFactory;
import io.inverno.core.compiler.spi.BeanQualifiedName;
import io.inverno.core.compiler.spi.BeanSocketQualifiedName;
import io.inverno.core.compiler.spi.ModuleBeanSocketInfo;
import io.inverno.core.compiler.spi.MultiSocketType;
import io.inverno.core.compiler.spi.SocketBeanInfo;
import java.util.Optional;
import java.util.function.Supplier;
import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.ModuleElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;
import javax.tools.Diagnostic.Kind;

/**
 * <p>
 * Factory used by {@link ModuleBeanInfoFactory} to create {@link ModuleBeanSocketInfo}.
 * </p>
 * 
 * @author <a href="mailto:jeremy.kuhn@inverno.io">Jeremy Kuhn</a>
 *
 */
class ModuleBeanSocketInfoFactory extends AbstractSocketInfoFactory {

	private final BeanQualifiedName beanQName;
	private final TypeMirror supplierType;
	private final NestedBeanInfoFactory nestedBeanFactory;
	
	/**
	 * 
	 */
	protected ModuleBeanSocketInfoFactory(ProcessingEnvironment processingEnvironment, ModuleElement moduleElement, BeanQualifiedName beanQName) {
		super(processingEnvironment, moduleElement);
		
		this.beanQName = beanQName;
		this.supplierType = this.processingEnvironment.getTypeUtils().erasure(this.processingEnvironment.getElementUtils().getTypeElement(Supplier.class.getCanonicalName()).asType());
		this.nestedBeanFactory = new NestedBeanInfoFactory(this.processingEnvironment);
	}

	public static ModuleBeanSocketInfoFactory create(ProcessingEnvironment processingEnvironment, ModuleElement moduleElement, BeanQualifiedName beanQName) {
		return new ModuleBeanSocketInfoFactory(processingEnvironment, moduleElement, beanQName);
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
	
	// Binary
	public ModuleBeanSocketInfo createBeanSocket(BeanQualifiedName beanQName, SocketBeanInfo moduleSocketInfo) {
		// TODO it would be better to use the actual beanQName but this broke cycle reporting 
//		BeanSocketQualifiedName socketQName = new BeanSocketQualifiedName(beanQName, moduleSocketInfo.getQualifiedName().getSimpleValue());
		BeanSocketQualifiedName socketQName = new BeanSocketQualifiedName(moduleSocketInfo.getQualifiedName(), moduleSocketInfo.getQualifiedName().getSimpleValue());
		return new CommonModuleBeanSingleSocketInfo(this.processingEnvironment, this.moduleElement, socketQName, moduleSocketInfo.getType(), null, null, moduleSocketInfo.isOptional(), false);
	}
	
	public SocketBeanInfo createMutatingSocketBean(BeanQualifiedName beanQName, CompiledMutatorBeanInfo mutatorBean, TypeMirror socketType) {
		AnnotationMirror[] selectors = mutatorBean.getElement().getAnnotationMirrors().stream().filter(a -> a.getAnnotationType().asElement().getAnnotation(Selector.class) != null).toArray(AnnotationMirror[]::new);
		
		MultiSocketType multiType = this.getMultiType(socketType);
		if(multiType != null) {
			return new CompiledMutatingMultiSocketInfo(this.processingEnvironment, mutatorBean.getElement(), mutatorBean.getAnnotation(), mutatorBean.getQualifiedName(), this.getComponentType(socketType), mutatorBean.getMutatorType(), selectors, mutatorBean.isRequired(), true, multiType);
		}
		else {
			CompiledMutatingSingleSocketInfo socketBeanInfo = new CompiledMutatingSingleSocketInfo(this.processingEnvironment, mutatorBean.getElement(), mutatorBean.getAnnotation(), mutatorBean.getQualifiedName(), socketType, mutatorBean.getMutatorType(), selectors, mutatorBean.isRequired(), true);
			socketBeanInfo.setNestedBeans(this.nestedBeanFactory.create(socketBeanInfo));
			return socketBeanInfo;
		}
	}
}
