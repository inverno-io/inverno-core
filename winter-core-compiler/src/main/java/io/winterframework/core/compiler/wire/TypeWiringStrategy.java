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
package io.winterframework.core.compiler.wire;

import java.util.Collection;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.type.ArrayType;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.type.WildcardType;

import io.winterframework.core.compiler.spi.BeanInfo;
import io.winterframework.core.compiler.spi.ModuleBeanInfo;
import io.winterframework.core.compiler.spi.ModuleQualifiedName;
import io.winterframework.core.compiler.spi.MultiSocketInfo;
import io.winterframework.core.compiler.spi.SocketInfo;
import io.winterframework.core.compiler.spi.WiringStrategy;

/**
 * <p>
 * {@link WiringStrategy} implementation used to determine if a bean is wirable
 * to a socket based on its type.
 * </p>
 * 
 * @author jkuhn
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
			if(this.isAssignable(((ModuleBeanInfo)bean).getProvidedType(), socket)) {
				return true;
			}
			return false;
		}
		else {
			return this.isAssignable(bean.getType(), socket);
		}
	}

	private boolean isAssignable(TypeMirror type, SocketInfo socket) {
		if(MultiSocketInfo.class.isAssignableFrom(socket.getClass())) {
			DeclaredType socketCollectionType = this.processingEnvironment.getTypeUtils().getDeclaredType(this.processingEnvironment.getElementUtils().getTypeElement(Collection.class.getCanonicalName()), socket.getType());
			ArrayType socketArrayType;
			if(!socket.getType().getKind().equals(TypeKind.WILDCARD)) {
				socketArrayType = this.processingEnvironment.getTypeUtils().getArrayType(socket.getType());
			}
			else {
				socketArrayType = this.processingEnvironment.getTypeUtils().getArrayType(((WildcardType)socket.getType()).getExtendsBound());
			}
			return this.processingEnvironment.getTypeUtils().isAssignable(type, socket.getType()) || this.processingEnvironment.getTypeUtils().isAssignable(type, socketArrayType) || this.processingEnvironment.getTypeUtils().isAssignable(type, socketCollectionType);
		}
		else {
			// Single socket
			return this.processingEnvironment.getTypeUtils().isAssignable(type, socket.getType());
		}
	}
}
