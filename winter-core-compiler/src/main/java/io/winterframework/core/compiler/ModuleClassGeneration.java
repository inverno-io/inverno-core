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

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.type.ArrayType;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.type.WildcardType;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;

import io.winterframework.core.compiler.spi.ModuleQualifiedName;
import io.winterframework.core.compiler.spi.MultiSocketType;

/**
 * <p>
 * Represents a module class generation which is used as a context by the
 * {@link ModuleClassGenerator} during the generation of a Winter module class.
 * </p>
 * 
 * @author jkuhn
 *
 */
class ModuleClassGeneration {

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
		IMPORT_MODULE_FIELD,
		IMPORT_MODULE_NEW,
		IMPORT_BEAN_REFERENCE;
	}
	
	private GenerationMode mode;
	
	private ModuleQualifiedName moduleQualifiedName;
	
	private Map<String, String> imports;
	
	private ProcessingEnvironment processingEnvironment;
	
	private int indentDepth = 0;
	
	private static final String DEFAULT_INDENT = "\t";
	
	private String indent = DEFAULT_INDENT;
	
	public ModuleClassGeneration(ProcessingEnvironment processingEnvironment, GenerationMode mode) {
		this.imports = new HashMap<>();
		this.processingEnvironment = processingEnvironment;
		this.mode = mode;
	}
	
	private ModuleClassGeneration(ModuleClassGeneration parentGeneration, GenerationMode mode) {
		this.imports = parentGeneration.imports;
		this.processingEnvironment = parentGeneration.processingEnvironment;
		this.mode = mode;
		this.indentDepth = parentGeneration.indentDepth;
		this.moduleQualifiedName = parentGeneration.getModule();
	}
	
	private ModuleClassGeneration(ModuleClassGeneration parentGeneration, int indentDepth) {
		this.imports = parentGeneration.imports;
		this.processingEnvironment = parentGeneration.processingEnvironment;
		this.mode = parentGeneration.getMode();
		this.indentDepth = indentDepth;
		this.moduleQualifiedName = parentGeneration.getModule();
	}
	
	private ModuleClassGeneration(ModuleClassGeneration parentGeneration, ModuleQualifiedName moduleQualifiedName) {
		this.imports = parentGeneration.imports;
		this.processingEnvironment = parentGeneration.processingEnvironment;
		this.mode = parentGeneration.getMode();
		this.indentDepth = parentGeneration.indentDepth;
		this.moduleQualifiedName = moduleQualifiedName;
	}
	
	public void setIndent(String indent) {
		this.indent = indent;
	}
	
	public String indent(int depth) {
		String repeatIndent = "";
		for(int i=0;i<this.indentDepth + depth;i++) {
			repeatIndent += this.indent;
		}
		return repeatIndent;
	}
	
	public ModuleClassGeneration withMode(GenerationMode mode) {
		return new ModuleClassGeneration(this, mode);
	}
	
	public ModuleClassGeneration withIndentDepth(int indentDepth) {
		return new ModuleClassGeneration(this, indentDepth);
	}
	
	public ModuleClassGeneration forModule(ModuleQualifiedName moduleQualifiedName) {
		return new ModuleClassGeneration(this, moduleQualifiedName);
	}
	
	public ModuleQualifiedName getModule() {
		return this.moduleQualifiedName;
	}
	
	public GenerationMode getMode() {
		return this.mode;
	}
	
	public Types getTypeUtils() {
		return this.processingEnvironment.getTypeUtils();
	}
	
	public Elements getElementUtils() {
		return this.processingEnvironment.getElementUtils();
	}
	
	public void addImport(String className, String canonicalName) {
		if(!this.imports.containsKey(className)) {
			this.imports.put(className, canonicalName);
		}
	}
	
	public void removeImport(String className) {
		this.imports.remove(className);
	}
	
	public Set<String> getImports() {
		return new HashSet<>(this.imports.values());
	}
	
	private void addImport(TypeMirror type) {
		if(type.getKind().equals(TypeKind.ARRAY)) {
			this.addImport(((ArrayType)type).getComponentType());
		}
		else if(type.getKind().equals(TypeKind.DECLARED)) {
			Element importElement = this.processingEnvironment.getTypeUtils().asElement(this.processingEnvironment.getTypeUtils().erasure(type));
			
			if(!this.imports.containsKey(importElement.getSimpleName().toString())) {
				this.imports.put(importElement.getSimpleName().toString(), importElement.toString());
			}
			for(TypeMirror typeArgument : ((DeclaredType)type).getTypeArguments()) {
				this.addImport(typeArgument);
			}
		}
		else if(type.getKind().equals(TypeKind.WILDCARD)) {
			if(((WildcardType)type).getExtendsBound() != null) {
				this.addImport(((WildcardType)type).getExtendsBound());
			}
			else if(((WildcardType)type).getSuperBound() != null) {
				this.addImport(((WildcardType)type).getSuperBound());
			}
		}
	}
	
	private boolean isImported(TypeMirror type) {
		Element erasedElement = this.processingEnvironment.getTypeUtils().asElement(this.processingEnvironment.getTypeUtils().erasure(type));
		return this.imports.containsKey(erasedElement.getSimpleName().toString()) && this.imports.get(erasedElement.getSimpleName().toString()).equals(erasedElement.toString());
	}
	
	public String getTypeName(String canonicalName) {
		String packageName = canonicalName.lastIndexOf(".") != -1 ? canonicalName.substring(0, canonicalName.lastIndexOf(".")) : "";
		String className = canonicalName.substring(packageName.length() + 1);
		
		this.addImport(className, canonicalName);
		
		if(this.imports.containsKey(className) && this.imports.get(className).equals(canonicalName)) {
			return className;
		}
		return canonicalName;
	}
	
	public String getTypeName(TypeMirror type) {
		this.addImport(type);
		
		if(type.getKind().equals(TypeKind.ARRAY)) {
			return this.getTypeName(((ArrayType)type).getComponentType()) + "[]";
		}
		else if(type.getKind().equals(TypeKind.DECLARED)) {
			Element erasedElement = this.processingEnvironment.getTypeUtils().asElement(this.processingEnvironment.getTypeUtils().erasure(type));
			String typeName = this.isImported(type) ? erasedElement.getSimpleName().toString() : erasedElement.toString(); 
			if(((DeclaredType)type).getTypeArguments().size() > 0) {
				return typeName + "<" + ((DeclaredType)type).getTypeArguments().stream().map(t -> this.getTypeName(t)).collect(Collectors.joining(", ")) + ">";
			}
			else {
				return typeName;
			}
		}
		else if(type.getKind().equals(TypeKind.WILDCARD)) {
			if(((WildcardType)type).getExtendsBound() != null) {
				return "? extends " + this.getTypeName(((WildcardType)type).getExtendsBound());
			}
			else if(((WildcardType)type).getSuperBound() != null) {
				return "? extends " + this.getTypeName(((WildcardType)type).getSuperBound());
			}
			else {
				return "?";
			}
		}
		else {
			return type.toString();
		}
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
}
