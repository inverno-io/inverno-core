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
package io.inverno.core.compiler.common;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.ModuleElement;
import javax.lang.model.type.ArrayType;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.type.WildcardType;

import io.inverno.core.compiler.TypeErrorException;
import io.inverno.core.compiler.spi.MultiSocketType;

/**
 * <p>
 * Base class for socket info factories.
 * </p>
 * 
 * @author <a href="mailto:jeremy.kuhn@inverno.io">Jeremy Kuhn</a>
 *
 */
public abstract class AbstractSocketInfoFactory extends AbstractInfoFactory {

	private final TypeMirror collectionType;
	
	private final TypeMirror setType;
	
	private final TypeMirror listType;
	
	public AbstractSocketInfoFactory(ProcessingEnvironment processingEnvironment, ModuleElement moduleElement) {
		super(processingEnvironment, moduleElement);
		
		this.collectionType = this.processingEnvironment.getTypeUtils().erasure(this.processingEnvironment.getElementUtils().getTypeElement(Collection.class.getCanonicalName()).asType());
		this.setType = this.processingEnvironment.getTypeUtils().erasure(this.processingEnvironment.getElementUtils().getTypeElement(Set.class.getCanonicalName()).asType());
		this.listType = this.processingEnvironment.getTypeUtils().erasure(this.processingEnvironment.getElementUtils().getTypeElement(List.class.getCanonicalName()).asType());
	}

	protected MultiSocketType getMultiType(TypeMirror type) {
		// TODO When the type is in an unrequired module, this returns COLLECTION, apparently isSameType returns true...
		if(type.getKind().equals(TypeKind.ARRAY)) {
			return MultiSocketType.ARRAY;
		}
		else if(this.processingEnvironment.getTypeUtils().isSameType(this.collectionType, this.processingEnvironment.getTypeUtils().erasure(type))) {
			return MultiSocketType.COLLECTION;
		}
		else if(this.processingEnvironment.getTypeUtils().isSameType(this.setType, this.processingEnvironment.getTypeUtils().erasure(type))) {
			return MultiSocketType.SET;
		}
		else if(this.processingEnvironment.getTypeUtils().isSameType(this.listType, this.processingEnvironment.getTypeUtils().erasure(type))) {
			return MultiSocketType.LIST;
		}
		return null;
	}
	
	protected TypeMirror getComponentType(TypeMirror multiTypeType) {
		if(multiTypeType.getKind().equals(TypeKind.ARRAY)) {
			return ((ArrayType)multiTypeType).getComponentType();
		}
		else if(this.processingEnvironment.getTypeUtils().isSameType(this.processingEnvironment.getTypeUtils().erasure(multiTypeType), this.collectionType) ||
			this.processingEnvironment.getTypeUtils().isSameType(this.processingEnvironment.getTypeUtils().erasure(multiTypeType), this.setType) ||
			this.processingEnvironment.getTypeUtils().isSameType(this.processingEnvironment.getTypeUtils().erasure(multiTypeType), this.listType)) {
			return ((DeclaredType)multiTypeType).getTypeArguments().get(0);
		}
		return null;
	}
	
	protected void validateType(TypeMirror type) throws TypeErrorException {
		if(type == null) {
			return;
		}
		if(type.getKind().equals(TypeKind.ERROR)) {
			throw new TypeErrorException(type);
		}
		if(type.getKind().equals(TypeKind.DECLARED)) {
			for(TypeMirror typeArgument : ((DeclaredType)type).getTypeArguments()) {
				this.validateType(typeArgument);
			}
		}
		if(type.getKind().equals(TypeKind.WILDCARD)) {
			this.validateType(((WildcardType)type).getExtendsBound());
			this.validateType(((WildcardType)type).getSuperBound());
		}
	}
}
