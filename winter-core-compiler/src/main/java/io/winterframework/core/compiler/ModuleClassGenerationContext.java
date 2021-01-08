/*
 * Copyright 2019 Jeremy KUHN
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
package io.winterframework.core.compiler;

import java.util.function.Supplier;

import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;

import io.winterframework.core.compiler.spi.ModuleQualifiedName;
import io.winterframework.core.compiler.spi.MultiSocketType;
import io.winterframework.core.compiler.spi.support.AbstractSourceGenerationContext;

/**
 * <p>
 * Represents a module class generation which is used as a context by the
 * {@link ModuleClassGenerator} during the generation of a Winter module class.
 * </p>
 * 
 * @author jkuhn
 *
 */
class ModuleClassGenerationContext extends AbstractSourceGenerationContext<ModuleClassGenerationContext, ModuleClassGenerationContext.GenerationMode> {

	public static enum GenerationMode {
		MODULE_CLASS,
		MODULE_BUILDER_CLASS,
		MODULE_LINKER_CLASS,
		MODULE_IMPORT,
		BEAN_FIELD,
		BEAN_NEW,
		BEAN_ACCESSOR,
		BEAN_REFERENCE,
		SOCKET_PARAMETER,
		SOCKET_FIELD,
		SOCKET_ASSIGNMENT,
		SOCKET_INJECTOR,
		COMPONENT_MODULE_FIELD,
		COMPONENT_MODULE_NEW,
		COMPONENT_MODULE_BEAN_REFERENCE
	}
	
	private TypeMirror supplierType = this.typeUtils.erasure(this.elementUtils.getTypeElement(Supplier.class.getCanonicalName()).asType());
	
	public ModuleClassGenerationContext(Types typeUtils, Elements elementUtils, GenerationMode mode) {
		super(typeUtils, elementUtils, mode);
	}
	
	private ModuleClassGenerationContext(ModuleClassGenerationContext parentGeneration) {
		super(parentGeneration);
	}
	
	@Override
	public ModuleClassGenerationContext withMode(GenerationMode mode) {
		ModuleClassGenerationContext context = new ModuleClassGenerationContext(this);
		context.mode = mode;
		return context;
	}
	
	@Override
	public ModuleClassGenerationContext withIndentDepth(int indentDepth) {
		ModuleClassGenerationContext context = new ModuleClassGenerationContext(this);
		context.indentDepth = indentDepth;
		return context;
	}
	
	@Override
	public ModuleClassGenerationContext withModule(ModuleQualifiedName moduleQualifiedName) {
		ModuleClassGenerationContext context = new ModuleClassGenerationContext(this);
		context.moduleQualifiedName = moduleQualifiedName;
		return context;
	}
	
	public String getMultiTypeName(TypeMirror type, MultiSocketType multiType) {
		if(multiType.equals(MultiSocketType.ARRAY)) {
			return this.getTypeName(this.getTypeUtils().getArrayType(type));
		}
		else if(multiType.equals(MultiSocketType.COLLECTION)) {
			return this.getTypeName(this.getTypeUtils().getDeclaredType(this.getElementUtils().getTypeElement("java.util.Collection"), type));
		}
		else if(multiType.equals(MultiSocketType.LIST)) {
			return this.getTypeName(this.getTypeUtils().getDeclaredType(this.getElementUtils().getTypeElement("java.util.List"), type));
		}
		else if(multiType.equals(MultiSocketType.SET)) {
			return this.getTypeName(this.getTypeUtils().getDeclaredType(this.getElementUtils().getTypeElement("java.util.Set"), type));
		}
		throw new IllegalArgumentException("Unexpected multi type: " + multiType);
	}
	
	public TypeMirror getSupplierSocketType(TypeMirror socketType) {
		if(this.typeUtils.isSameType(this.typeUtils.erasure(socketType), this.supplierType)) {
			return socketType;
		}
		else {
			return ((TypeElement)this.typeUtils.asElement(socketType)).getInterfaces().stream().filter(type -> this.typeUtils.isSameType(this.typeUtils.erasure(type), this.supplierType)).findFirst().orElseThrow(() -> new IllegalStateException("Socket type does not extend " + Supplier.class.getCanonicalName()));
		}
	}
}
