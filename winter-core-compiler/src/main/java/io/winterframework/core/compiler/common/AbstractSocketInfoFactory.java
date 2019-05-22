/**
 * 
 */
package io.winterframework.core.compiler.common;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.ModuleElement;
import javax.lang.model.type.ArrayType;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;

import io.winterframework.core.compiler.spi.MultiSocketType;

/**
 * @author jkuhn
 *
 */
public abstract class AbstractSocketInfoFactory extends AbstractInfoFactory {

	private TypeMirror collectionType;
	
	private TypeMirror setType;
	
	private TypeMirror listType;
	
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
}
