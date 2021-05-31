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
package io.inverno.core.compiler.wire;

import java.util.Collection;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.type.ArrayType;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.type.WildcardType;

import io.inverno.core.compiler.spi.BeanInfo;
import io.inverno.core.compiler.spi.ModuleBeanInfo;
import io.inverno.core.compiler.spi.ModuleQualifiedName;
import io.inverno.core.compiler.spi.MultiSocketInfo;
import io.inverno.core.compiler.spi.SocketInfo;
import io.inverno.core.compiler.spi.WiringStrategy;

/**
 * <p>
 * {@link WiringStrategy} implementation used to determine if a bean is wirable
 * to a socket based on its type.
 * </p>
 * 
 * @author <a href="mailto:jeremy.kuhn@inverno.io">Jeremy Kuhn</a>
 *
 */
public class TypeWiringStrategy implements WiringStrategy {

	private ProcessingEnvironment processingEnvironment;
	
	private ModuleQualifiedName moduleQName;
	
	public TypeWiringStrategy(ProcessingEnvironment processingEnvironment, ModuleQualifiedName moduleQName) {
		this.processingEnvironment = processingEnvironment;
		this.moduleQName = moduleQName;
	}
	
	
	@Override
	public boolean isWirable(BeanInfo bean, SocketInfo socket) {
		if(ModuleBeanInfo.class.isAssignableFrom(bean.getClass()) && !bean.getQualifiedName().getModuleQName().equals(this.moduleQName) && ((ModuleBeanInfo)bean).getProvidedType() != null) {
			return this.isAssignable(((ModuleBeanInfo)bean).getProvidedType(), socket);
		}
		else {
			return this.isAssignable(bean.getType(), socket);
		}
	}

	public boolean isAssignable(TypeMirror type, SocketInfo socket) {
		if(type.getKind().equals(TypeKind.WILDCARD)) {
			WildcardType wildcardType = (WildcardType)type;
			if(wildcardType.getExtendsBound() != null) {
				type = wildcardType.getExtendsBound();
			}
			// TODO maybe this shall be useful for now just keep things safe
			/*else if(wildcardType.getSuperBound() != null) {
				type = wildcardType.getSuperBound();
			}*/
			else {
				return false;
			}
		}
		if(MultiSocketInfo.class.isAssignableFrom(socket.getClass())) {
			DeclaredType socketCollectionType = this.processingEnvironment.getTypeUtils().getDeclaredType(this.processingEnvironment.getElementUtils().getTypeElement(Collection.class.getCanonicalName()), socket.getType());
			ArrayType socketArrayType;
			if(!socket.getType().getKind().equals(TypeKind.WILDCARD)) {
				socketArrayType = this.processingEnvironment.getTypeUtils().getArrayType(socket.getType());
			}
			else {
				WildcardType socketWildcardType = (WildcardType)socket.getType();
				socketArrayType = this.processingEnvironment.getTypeUtils().getArrayType(socketWildcardType.getExtendsBound() != null ? socketWildcardType.getExtendsBound() : socketWildcardType.getSuperBound());
			}
			return this.processingEnvironment.getTypeUtils().isAssignable(type, socket.getType()) || (socketArrayType != null && this.processingEnvironment.getTypeUtils().isAssignable(type, socketArrayType)) || this.processingEnvironment.getTypeUtils().isAssignable(type, socketCollectionType);
		}
		else {
			if(!socket.getType().getKind().equals(TypeKind.WILDCARD)) {
				return this.processingEnvironment.getTypeUtils().isAssignable(type, socket.getType());
			}
			else {
				// This should never happen for "regular" single sockets
				return false;
			}
		}
	}
}
