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
package io.inverno.core.compiler;

import java.util.function.Supplier;

import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;

import io.inverno.core.compiler.spi.ModuleQualifiedName;
import io.inverno.core.compiler.spi.MultiSocketType;
import io.inverno.core.compiler.spi.support.AbstractSourceGenerationContext;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * <p>
 * Represents a generation context used by the {@link ModuleClassGenerator} during the generation of an Inverno module class.
 * </p>
 * 
 * @author <a href="mailto:jeremy.kuhn@inverno.io">Jeremy Kuhn</a>
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
		BEAN_OPTIONAL_REFERENCE,
		SOCKET_PARAMETER,
		SOCKET_FIELD,
		SOCKET_ASSIGNMENT,
		SOCKET_INJECTOR,
		COMPONENT_MODULE_FIELD,
		COMPONENT_MODULE_NEW,
		COMPONENT_MODULE_BEAN_REFERENCE
	}
	
	private TypeMirror supplierType;
	
	private String optionalTypeName;
	private String supplierTypeName;
	private String mapTypeName;
	private String listTypeName;
	private String setTypeName;
	private String npeTypeName;
	
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
		switch (multiType) {
			case ARRAY:
				return this.getTypeName(this.getTypeUtils().getArrayType(type));
			case COLLECTION:
				return this.getTypeName(this.getTypeUtils().getDeclaredType(this.getElementUtils().getTypeElement("java.util.Collection"), type));
			case LIST:
				return this.getTypeName(this.getTypeUtils().getDeclaredType(this.getElementUtils().getTypeElement("java.util.List"), type));
			case SET:
				return this.getTypeName(this.getTypeUtils().getDeclaredType(this.getElementUtils().getTypeElement("java.util.Set"), type));
			default:
				break;
		}
		throw new IllegalArgumentException("Unexpected multi type: " + multiType);
	}
	
	public TypeMirror getSupplierSocketType(TypeMirror socketType) {
		if(this.typeUtils.isSameType(this.typeUtils.erasure(socketType), this.getSupplierType())) {
			return socketType;
		}
		else {
			return ((TypeElement)this.typeUtils.asElement(socketType)).getInterfaces().stream().filter(type -> this.typeUtils.isSameType(this.typeUtils.erasure(type), this.getSupplierType())).findFirst().orElseThrow(() -> new IllegalStateException("Socket type does not extend " + Supplier.class.getCanonicalName()));
		}
	}
	
	private TypeMirror getSupplierType() {
		if(this.supplierType == null) {
			if(this.parentGeneration != null) {
				return this.parentGeneration.getSupplierType();
			}
			this.supplierType = this.typeUtils.erasure(this.elementUtils.getTypeElement(Supplier.class.getCanonicalName()).asType());
		}
		return this.supplierType;
	}
	
	public String getOptionalTypeName() {
		if(this.optionalTypeName == null) {
			if(this.parentGeneration != null) {
				return this.parentGeneration.getOptionalTypeName();
			}
			this.optionalTypeName = this.getTypeName(this.typeUtils.erasure(this.elementUtils.getTypeElement(Optional.class.getCanonicalName()).asType()));
		}
		return this.optionalTypeName;
	}
	
	public String getSupplierTypeName() {
		if(this.supplierTypeName == null) {
			if(this.parentGeneration != null) {
				return this.parentGeneration.getSupplierTypeName();
			}
			this.supplierTypeName = this.getTypeName(this.getSupplierType());
		}
		return this.supplierTypeName;
	}
	
	public String getMapTypeName() {
		if(this.mapTypeName == null) {
			if(this.parentGeneration != null) {
				return this.parentGeneration.getMapTypeName();
			}
			this.mapTypeName = this.getTypeName(this.typeUtils.erasure(this.elementUtils.getTypeElement(Map.class.getCanonicalName()).asType()));
		}
		return this.mapTypeName;
	}
	
	public String getListTypeName() {
		if(this.listTypeName == null) {
			if(this.parentGeneration != null) {
				return this.parentGeneration.getListTypeName();
			}
			this.listTypeName = this.getTypeName(this.typeUtils.erasure(this.elementUtils.getTypeElement(List.class.getCanonicalName()).asType()));
		}
		return this.listTypeName;
	}
	
	public String getSetTypeName() {
		if(this.setTypeName == null) {
			if(this.parentGeneration != null) {
				return this.parentGeneration.getSetTypeName();
			}
			this.setTypeName = this.getTypeName(this.typeUtils.erasure(this.elementUtils.getTypeElement(Set.class.getCanonicalName()).asType()));
		}
		return this.setTypeName;
	}
	
	public String getNpeTypeName() {
		if(this.npeTypeName == null) {
			if(this.parentGeneration != null) {
				return this.parentGeneration.getNpeTypeName();
			}
			this.npeTypeName = this.getTypeName(this.elementUtils.getTypeElement(NullPointerException.class.getCanonicalName()).asType());
		}
		return this.npeTypeName;
	}
}
