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

import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;

import io.inverno.core.compiler.spi.ModuleQualifiedName;
import io.inverno.core.compiler.spi.MultiSocketType;
import io.inverno.core.compiler.spi.support.AbstractSourceGenerationContext;
import java.util.Collection;
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

	public enum GenerationMode {
		MODULE_CLASS,
		MODULE_BUILDER_CLASS,
		MODULE_LINKER_CLASS,
		BEAN_FIELD,
		BEAN_NEW,
		BEAN_ACCESSOR,
		BEAN_REFERENCE,
		BEAN_OPTIONAL_REFERENCE,
		SOCKET_PARAMETER,
		SOCKET_FIELD,
		SOCKET_ASSIGNMENT,
		SOCKET_INJECTOR,
		SOCKET_SUPPLIER,
		COMPONENT_MODULE_FIELD,
		COMPONENT_MODULE_NEW,
		COMPONENT_MODULE_BEAN_REFERENCE
	}
	
	private final TypeMirror supplierType;
	private final String optionalTypeName;
	private final String supplierTypeName;
	private final String mapTypeName;
	private final String collectionTypeName;
	private final String listTypeName;
	private final String setTypeName;
	private final String npeTypeName;
	
	public ModuleClassGenerationContext(Types typeUtils, Elements elementUtils, GenerationMode mode) {
		super(typeUtils, elementUtils, mode);

		this.supplierType = this.typeUtils.erasure(this.elementUtils.getTypeElement(Supplier.class.getCanonicalName()).asType());
		this.optionalTypeName = this.getTypeName(this.typeUtils.erasure(this.elementUtils.getTypeElement(Optional.class.getCanonicalName()).asType()));
		this.supplierTypeName = this.getTypeName(this.supplierType);
		this.mapTypeName = this.getTypeName(this.typeUtils.erasure(this.elementUtils.getTypeElement(Map.class.getCanonicalName()).asType()));
		this.collectionTypeName = this.getTypeName(this.typeUtils.erasure(this.elementUtils.getTypeElement(Collection.class.getCanonicalName()).asType()));
		this.listTypeName = this.getTypeName(this.typeUtils.erasure(this.elementUtils.getTypeElement(List.class.getCanonicalName()).asType()));
		this.setTypeName = this.getTypeName(this.typeUtils.erasure(this.elementUtils.getTypeElement(Set.class.getCanonicalName()).asType()));
		this.npeTypeName = this.getTypeName(this.elementUtils.getTypeElement(NullPointerException.class.getCanonicalName()).asType());
	}
	
	private ModuleClassGenerationContext(ModuleClassGenerationContext parentGeneration) {
		super(parentGeneration);

		this.supplierType = parentGeneration.supplierType;
		this.optionalTypeName = parentGeneration.optionalTypeName;
		this.supplierTypeName = parentGeneration.supplierTypeName;
		this.mapTypeName = parentGeneration.mapTypeName;
		this.collectionTypeName = parentGeneration.collectionTypeName;
		this.listTypeName = parentGeneration.listTypeName;
		this.setTypeName = parentGeneration.setTypeName;
		this.npeTypeName = parentGeneration.npeTypeName;
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
				return this.getTypeName(this.getTypeUtils().getDeclaredType(this.getElementUtils().getTypeElement(Collection.class.getCanonicalName()), type));
			case LIST:
				return this.getTypeName(this.getTypeUtils().getDeclaredType(this.getElementUtils().getTypeElement(List.class.getCanonicalName()), type));
			case SET:
				return this.getTypeName(this.getTypeUtils().getDeclaredType(this.getElementUtils().getTypeElement(Set.class.getCanonicalName()), type));
			default:
				break;
		}
		throw new IllegalArgumentException("Unexpected multi type: " + multiType);
	}
	
	private TypeMirror getSupplierType() {
		return this.supplierType;
	}
	
	public String getOptionalTypeName() {
		return this.optionalTypeName;
	}
	
	public String getSupplierTypeName() {
		return this.supplierTypeName;
	}
	
	public String getMapTypeName() {
		return this.mapTypeName;
	}
	
	public String getCollectionTypeName() {
		return this.collectionTypeName;
	}
	
	public String getListTypeName() {
		return this.listTypeName;
	}
	
	public String getSetTypeName() {
		return this.setTypeName;
	}
	
	public String getNpeTypeName() {
		return this.npeTypeName;
	}
}
