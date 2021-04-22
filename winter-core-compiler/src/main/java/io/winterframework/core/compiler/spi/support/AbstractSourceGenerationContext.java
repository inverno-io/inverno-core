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
import io.winterframework.core.compiler.spi.QualifiedName;

/**
 * <p>
 * Represents a source class generation context which provides utilities aimed
 * to simplify the generation of Java source classes.
 * </p>
 * 
 * @author <a href="mailto:jeremy.kuhn@winterframework.io">Jeremy Kuhn</a>
 *
 */
public abstract class AbstractSourceGenerationContext<A extends AbstractSourceGenerationContext<A,B>, B extends Enum<B>> {

	/**
	 * The parent generation context.
	 */
	protected A parentGeneration;
	
	/**
	 * The types utility.
	 */
	protected Types typeUtils;
	
	/**
	 * The elements utility.
	 */
	protected Elements elementUtils;
	
	/**
	 * The generation mode.
	 */
	protected B mode;
	
	/**
	 * The map of imports.
	 */
	protected Map<String, String> imports;
	
	/**
	 * The indent depth.
	 */
	protected int indentDepth = 0;
	
	/**
	 * The default indent.
	 */
	protected static final String DEFAULT_INDENT = "\t";
	
	/**
	 * The indent used during generation.
	 */
	protected String indent;
	
	/**
	 * The qualified name of the module being generated.
	 */
	protected ModuleQualifiedName moduleQualifiedName;
	
	/**
	 * The map of field names used to avoid duplicates.
	 */
	protected Map<QualifiedName, String> fieldNames;
	
	/**
	 * <p>
	 * Creates a source generation context.
	 * </p>
	 * 
	 * @param typeUtils    the types utility
	 * @param elementUtils the elements utility
	 * @param mode         the generation mode
	 */
	public AbstractSourceGenerationContext(Types typeUtils, Elements elementUtils, B mode) {
		this(typeUtils, elementUtils, mode, DEFAULT_INDENT);
	}
	
	/**
	 * <p>
	 * Creates a source generation context.
	 * </p>
	 * 
	 * @param typeUtils    the types utility
	 * @param elementUtils the elements utility
	 * @param mode         the generation mode
	 * @param indent       the indent
	 */
	public AbstractSourceGenerationContext(Types typeUtils, Elements elementUtils, B mode, String indent) {
		this.imports = new HashMap<>();
		this.typeUtils = typeUtils;
		this.elementUtils = elementUtils;
		this.mode = mode;
		this.setIndent(indent);
		this.fieldNames = new HashMap<>();
	}
	
	/**
	 * <p>
	 * Creates a source generation context from a parent generation context.
	 * </p>
	 * 
	 * @param parentGeneration the parent generation context
	 */
	protected AbstractSourceGenerationContext(A parentGeneration) {
		this.parentGeneration = parentGeneration;
		this.imports = parentGeneration.imports;
		this.typeUtils = parentGeneration.typeUtils;
		this.elementUtils = parentGeneration.elementUtils;
		this.mode = parentGeneration.getMode();
		this.setIndent(parentGeneration.indent);
		this.indentDepth = parentGeneration.indentDepth;
		this.moduleQualifiedName = parentGeneration.moduleQualifiedName;
		this.fieldNames = parentGeneration.fieldNames;
	}
	
	/**
	 * <p>
	 * Sets the generation indent.
	 * </p>
	 * 
	 * @param indent an indent
	 */
	public void setIndent(String indent) {
		this.indent = indent;
	}
	
	/**
	 * <p>
	 * Returns an indent of the specified depth from the current generation indent
	 * depth.
	 * </p>
	 * 
	 * @param depth the relative indent depth
	 * 
	 * @return an indent
	 */
	public String indent(int depth) {
		String repeatIndent = "";
		for(int i=0;i<this.indentDepth + depth;i++) {
			repeatIndent += this.indent;
		}
		return repeatIndent;
	}
	
	/**
	 * <p>
	 * Returns a new generation context created from this context with the specified
	 * mode.
	 * </p>
	 * 
	 * <p>
	 * This generation context remains untouched.
	 * </p>
	 * 
	 * @param mode a generation mode
	 * 
	 * @return a new generation context
	 */
	public abstract A withMode(B mode);
	
	/**
	 * <p>
	 * Returns a new generation context created from this context with an indent
	 * depth increased by the specified delta.
	 * </p>
	 * 
	 * <p>
	 * This generation context remains untouched.
	 * </p>
	 * 
	 * @param delta the indent depth delta
	 * 
	 * @return a new generation context
	 */
	public A withIndentDepthAdd(int delta) {
		return this.withIndentDepth(this.indentDepth + delta);
	}
	
	/**
	 * <p>
	 * Returns a new generation context created from this context with the specified
	 * indent depth.
	 * </p>
	 * 
	 * <p>
	 * This generation context remains untouched.
	 * </p>
	 * 
	 * @param indentDepth the indent depth
	 * 
	 * @return a new generation context
	 */
	public abstract A withIndentDepth(int indentDepth);
	
	/**
	 * <p>
	 * Returns a new generation context created from this context for the specified
	 * module.
	 * </p>
	 * 
	 * <p>
	 * This generation context remains untouched.
	 * </p>
	 * 
	 * @param moduleQualifiedName a module qualified name
	 * 
	 * @return a new generation context
	 */
	public abstract A withModule(ModuleQualifiedName moduleQualifiedName);
	
	/**
	 * <p>
	 * Returns the generation mode.
	 * </p>
	 * 
	 * @return the generation mode
	 */
	public B getMode() {
		return this.mode;
	}
	
	/**
	 * <p>
	 * Returns the indent depth.
	 * </p>
	 * 
	 * @return the indent depth
	 */
	public int getIndentDepth() {
		return indentDepth;
	}
	
	/**
	 * <p>
	 * Returns the generated module.
	 * </p>
	 * 
	 * @return the generated module qualified name
	 */
	public ModuleQualifiedName getModule() {
		return this.moduleQualifiedName;
	}
	
	/**
	 * <p>
	 * Returns the types utility.
	 * </p>
	 * 
	 * @return the types utility
	 */
	public Types getTypeUtils() {
		return this.typeUtils;
	}
	
	/**
	 * <p>
	 * Returns the elements utility.
	 * </p>
	 * 
	 * @return the elements utility
	 */
	public Elements getElementUtils() {
		return this.elementUtils;
	}
	
	/**
	 * <p>
	 * Adds the specified import to the context.
	 * </p>
	 * 
	 * @param className     the simple name of the imported type
	 * @param canonicalName the canonical name of the imported type
	 */
	public void addImport(String className, String canonicalName) {
		if(!this.imports.containsKey(className)) {
			this.imports.put(className, canonicalName);
		}
	}
	
	/**
	 * <p>
	 * Removes the import for the specified class.
	 * </p>
	 * 
	 * @param className a simple class name
	 */
	public void removeImport(String className) {
		this.imports.remove(className);
	}
	
	/**
	 * <p>
	 * Returns the list of imports.
	 * </p>
	 * 
	 * @return a set of imports
	 */
	public Set<String> getImports() {
		return new HashSet<>(this.imports.values());
	}

	/**
	 * <p>
	 * Adds the specified import to the context.
	 * </p>
	 * 
	 * @param type the imported type of the imported class
	 */
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
	
	/**
	 * <p>
	 * Determines whether the specified type is imported.
	 * </p>
	 * 
	 * @param type a type
	 * @return true if the type is imported, false otherwise
	 */
	private boolean isImported(TypeMirror type) {
		Element erasedElement = this.typeUtils.asElement(this.typeUtils.erasure(type));
		return this.imports.containsKey(erasedElement.getSimpleName().toString()) && this.imports.get(erasedElement.getSimpleName().toString()).equals(erasedElement.toString());
	}
	
	/**
	 * <p>
	 * Adds the specified canonical name to the list of imports if not done yet and
	 * returns the simple type name.
	 * </p>
	 * 
	 * @param canonicalName a canonical name
	 * @return the simple type name
	 */
	public String getTypeName(String canonicalName) {
		String packageName = canonicalName.lastIndexOf(".") != -1 ? canonicalName.substring(0, canonicalName.lastIndexOf(".")) : "";
		String className = canonicalName.substring(packageName.length() + 1);
		
		this.addImport(className, canonicalName);
		
		if(this.imports.containsKey(className) && this.imports.get(className).equals(canonicalName)) {
			return className;
		}
		return canonicalName;
	}
	
	/**
	 * <p>
	 * Adds the specified type to the list of imports if not done yet and returns
	 * the simple type name.
	 * </p>
	 * 
	 * @param type a type
	 * @return the simple type name
	 */
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
	
	/**
	 * <p>
	 * Returns a unique field name corresponding to the specified qualified name.
	 * </p>
	 * 
	 * @param qName a qualified name
	 * 
	 * @return a unique field name
	 */
	public String getFieldName(QualifiedName qName) {
		String fieldName = this.fieldNames.get(qName);
		if(fieldName != null) {
			return fieldName;
		}

		String normalizedQName = qName.normalize();
		fieldName = normalizedQName;
		int index = 1;
		while(this.fieldNames.containsValue(fieldName)) {
			fieldName = normalizedQName + "_" + index;
			index++;
		}
		this.fieldNames.put(qName, fieldName);
		return fieldName;
	}
	
	/**
	 * <p>
	 * Returns a Collector that concatenates the input elements into a
	 * StringBuilder, in encounter order.
	 * </p>
	 * 
	 * @return a Collector that concatenates the input elements into a
	 *         StringBuilder, in encounter order
	 */
	public Collector<CharSequence, ?, StringBuilder> joining() {
		return Collector.of(
				StringBuilder::new, 
				(stringBuilder, seq) -> stringBuilder.append(seq),
				StringBuilder::append, 
				stringBuilder -> stringBuilder
			);
	}
	
	/**
	 * <p>
	 * Returns a Collector that concatenates the input elements, separated by the
	 * specified delimiter, in encounter order.
	 * </p>
	 * 
	 * @param delimiter the delimiter to be used between each element
	 * 
	 * @return A Collector which concatenates CharSequence elements, separated by
	 *         the specified delimiter, in encounter order
	 */
	public Collector<CharSequence, ?, StringBuilder> joining(CharSequence delimiter) {
		return Collector.of(
			StringBuilder::new, 
			(stringBuilder, seq) -> stringBuilder.append(seq).append(delimiter),
			StringBuilder::append, 
			stringBuilder -> stringBuilder.length() > 0 ? stringBuilder.delete(stringBuilder.length() - delimiter.length(), stringBuilder.length()) : stringBuilder
		);
	}
}
