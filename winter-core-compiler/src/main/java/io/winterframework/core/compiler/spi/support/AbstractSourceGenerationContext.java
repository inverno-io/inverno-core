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
package io.winterframework.core.compiler.spi.support;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collector;
import java.util.stream.Collectors;

import javax.lang.model.element.Element;
import javax.lang.model.type.ArrayType;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.type.WildcardType;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;

import io.winterframework.core.compiler.spi.ModuleQualifiedName;

/**
 * <p>
 * Represents a source class generation context which provides utilities aimed
 * to simplify the generation of Java source classes.
 * </p>
 * 
 * @author jkuhn
 *
 */
public abstract class AbstractSourceGenerationContext<A extends AbstractSourceGenerationContext<A,B>, B extends Enum<B>> {

	protected A parentGeneration;
	
	protected Types typeUtils;
	
	protected Elements elementUtils;
	
	protected B mode;
	
	protected Map<String, String> imports;
	
	protected int indentDepth = 0;
	
	protected static final String DEFAULT_INDENT = "\t";
	
	protected String indent = DEFAULT_INDENT;
	
	protected ModuleQualifiedName moduleQualifiedName;
	
	public AbstractSourceGenerationContext(Types typeUtils, Elements elementUtils, B mode) {
		this.imports = new HashMap<>();
		this.typeUtils = typeUtils;
		this.elementUtils = elementUtils;
		this.mode = mode;
	}
	
	protected AbstractSourceGenerationContext(A parentGeneration) {
		this.parentGeneration = parentGeneration;
		this.imports = parentGeneration.imports;
		this.typeUtils = parentGeneration.typeUtils;
		this.elementUtils = parentGeneration.elementUtils;
		this.mode = parentGeneration.getMode();
		this.indentDepth = parentGeneration.indentDepth;
		this.moduleQualifiedName = parentGeneration.moduleQualifiedName;
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
	
	public abstract A withMode(B mode);
	
	public abstract A withIndentDepth(int indentDepth);
	
	public abstract A withModule(ModuleQualifiedName moduleQualifiedName);
	
	public B getMode() {
		return this.mode;
	}
	
	public int getIndentDepth() {
		return indentDepth;
	}
	
	public ModuleQualifiedName getModule() {
		return this.moduleQualifiedName;
	}
	
	public Types getTypeUtils() {
		return this.typeUtils;
	}
	
	public Elements getElementUtils() {
		return this.elementUtils;
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
			Element importElement = this.typeUtils.asElement(this.typeUtils.erasure(type));
			
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
		Element erasedElement = this.typeUtils.asElement(this.typeUtils.erasure(type));
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
			Element erasedElement = this.typeUtils.asElement(this.typeUtils.erasure(type));
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
	
	public Collector<CharSequence, ?, StringBuilder> joining(CharSequence delimiter) {
		return Collector.of(
			StringBuilder::new, 
			(stringBuilder, seq) -> stringBuilder.append(seq).append(delimiter),
			StringBuilder::append, 
			stringBuilder -> stringBuilder.length() > 0 ? stringBuilder.delete(stringBuilder.length() - delimiter.length(), stringBuilder.length()) : stringBuilder
		);
	}
}
