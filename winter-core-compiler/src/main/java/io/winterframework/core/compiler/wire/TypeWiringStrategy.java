/**
 * 
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
