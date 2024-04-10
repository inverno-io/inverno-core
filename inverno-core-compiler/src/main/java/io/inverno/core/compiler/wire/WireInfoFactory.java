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

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.ModuleElement;
import javax.lang.model.type.TypeMirror;

import io.inverno.core.annotation.Wire;
import io.inverno.core.annotation.Wires;
import io.inverno.core.compiler.common.AbstractInfoFactory;
import io.inverno.core.compiler.spi.BeanInfo;
import io.inverno.core.compiler.spi.BeanQualifiedName;
import io.inverno.core.compiler.spi.BeanSocketQualifiedName;
import io.inverno.core.compiler.spi.ModuleBeanSocketInfo;
import io.inverno.core.compiler.spi.QualifiedNameFormatException;
import io.inverno.core.compiler.spi.ReporterInfo;
import io.inverno.core.compiler.spi.SocketBeanInfo;

/**
 * <p>
 * Used when building a compiled module to create wire info from the compiled module element.
 * </p>
 * 
 * @author <a href="mailto:jeremy.kuhn@inverno.io">Jeremy Kuhn</a>
 *
 */
public class WireInfoFactory extends AbstractInfoFactory {

	private final Map<BeanQualifiedName, ? extends BeanInfo> beans;
	
	private final Set<BeanSocketQualifiedName> beanSockets;
	
	private final Map<BeanQualifiedName, ? extends SocketBeanInfo> requiredModuleSockets;
	
	private final TypeMirror wireAnnotationType;
	private final TypeMirror wiresAnnotationType;
	
	protected WireInfoFactory(ProcessingEnvironment processingEnvironment, ModuleElement moduleElement, List<? extends BeanInfo> beans, List<? extends ModuleBeanSocketInfo> beanSockets, List<? extends SocketBeanInfo> requiredModuleSockets) {
		super(processingEnvironment, moduleElement);
		this.processingEnvironment = processingEnvironment;
		this.moduleElement = moduleElement;
		
		this.beans = beans.stream().collect(Collectors.toMap(beanInfo -> beanInfo.getQualifiedName(), Function.identity()));
		this.beanSockets = beanSockets.stream().map(ModuleBeanSocketInfo::getQualifiedName).collect(Collectors.toSet());
		
		this.requiredModuleSockets = requiredModuleSockets.stream().collect(Collectors.toMap(moduleSocketInfo -> moduleSocketInfo.getQualifiedName(), Function.identity()));
		
		this.wireAnnotationType = this.processingEnvironment.getElementUtils().getTypeElement(Wire.class.getCanonicalName()).asType();
		this.wiresAnnotationType = this.processingEnvironment.getElementUtils().getTypeElement(Wires.class.getCanonicalName()).asType();
	}
	
	public static WireInfoFactory create(ProcessingEnvironment processingEnvironment, ModuleElement moduleElement, List<? extends BeanInfo> beans, List<? extends ModuleBeanSocketInfo> beanSockets, List<? extends SocketBeanInfo> requiredModuleSockets) {
		return new WireInfoFactory(processingEnvironment, moduleElement, beans,beanSockets, requiredModuleSockets);
	}
	
	@SuppressWarnings("unchecked")
	public WireInfo<?> createWire(AnnotationMirror annotation) {
		if(!this.processingEnvironment.getTypeUtils().isSameType(annotation.getAnnotationType(), this.wireAnnotationType)) {
			throw new IllegalArgumentException("The specified annotation is not a @" + Wire.class.getSimpleName() + " annotation");
		}
		Optional<? extends AnnotationMirror> wireOnModule = this.moduleElement.getAnnotationMirrors().stream()
			.filter(a -> this.processingEnvironment.getTypeUtils().isSameType(a.getAnnotationType(), this.wireAnnotationType)).findFirst();
		
		if(wireOnModule.isPresent()) {
			if(!wireOnModule.get().equals(annotation)) {
				throw new IllegalArgumentException("The specified @" + Wire.class.getSimpleName() + " annotation hasn't been declared on the module to build");
			}
		}
		else {
			Optional<? extends AnnotationMirror> wiresOnModule = this.moduleElement.getAnnotationMirrors().stream()
				.filter(a -> this.processingEnvironment.getTypeUtils().isSameType(a.getAnnotationType(), this.wiresAnnotationType)).findFirst();
			
			if(!wiresOnModule.isPresent() || !((Collection<? extends AnnotationValue>)wiresOnModule.get().getElementValues().values().iterator().next().getValue()).stream().map(v -> (AnnotationMirror)v.getValue()).anyMatch(a -> a.equals(annotation))) {
				throw new IllegalArgumentException("The specified @" + Wire.class.getSimpleName() + " annotation hasn't been declared on the module to build");
			}
		}
		
		ReporterInfo wireReporter = this.getReporter(this.moduleElement, annotation);
		
		String[] beans = null;
		String into = null;
		for(Entry<? extends ExecutableElement, ? extends AnnotationValue> value : this.processingEnvironment.getElementUtils().getElementValuesWithDefaults(annotation).entrySet()) {
			switch(value.getKey().getSimpleName().toString()) {
				case "beans" : beans = ((List<AnnotationValue>)value.getValue().getValue()).stream().map(v -> v.getValue()).toArray(String[]::new);
					break;
				case "into" : into = (String)value.getValue().getValue();
					break;
			}
		}
		
		List<BeanQualifiedName> beanQNamesList = Arrays.stream(beans)
			.map(bean -> {
				try {
					return BeanQualifiedName.valueOf(bean);
				}
				catch(QualifiedNameFormatException e) {
					try {
						return new BeanQualifiedName(this.moduleQName, bean);
					}
					catch(QualifiedNameFormatException e1) {
						wireReporter.error("Invalid bean qualified name: " + bean + ", expecting (<moduleName>):<beanName> with valid Java identifiers");
						return null;
					}
				}
			})
			.filter(Objects::nonNull)
			.collect(Collectors.toList());
		
		String duplicateBeans = String.join(", ", beanQNamesList.stream().filter(beanQName -> Collections.frequency(beanQNamesList, beanQName) > 1).map(Object::toString).collect(Collectors.toSet()));
		if(duplicateBeans != null && !duplicateBeans.equals("")) {
			wireReporter.warning("The following beans are specified multiple times: " + duplicateBeans);
		}
		
		BeanQualifiedName[] beanQNames = new HashSet<>(beanQNamesList).stream().toArray(BeanQualifiedName[]::new);
		
		BeanSocketQualifiedName beanSocketQName = null;
		BeanQualifiedName moduleSocketQName = null;
		try {
			try {
				// BeanSocket with explicit module: <module>:<bean>:<socket>
				beanSocketQName = BeanSocketQualifiedName.valueOf(into);
			}
			catch(QualifiedNameFormatException e) {
				// BeanSocket with implicit module: <bean>:<socket>
				beanSocketQName = BeanSocketQualifiedName.valueOf(this.moduleQName, into);
			}

			if(!this.beanSockets.contains(beanSocketQName)) {
				// There's no bean socket with that name so let's try to interpret this as a ModuleSocket: <module>:<socket>
				beanSocketQName = null;
				throw new QualifiedNameFormatException();
			}
		}
		catch(QualifiedNameFormatException e) {
			// ModuleSocket <module>:<socket>
			try {
				moduleSocketQName = BeanQualifiedName.valueOf(into);
				if(moduleSocketQName.getModuleQName().equals(this.moduleQName)) {
					wireReporter.error("You can't wire beans to a socket bean defined in the same module");
					return null;
				}
			}
			catch(QualifiedNameFormatException e1) {
				wireReporter.error("Invalid socket qualified name: " + into + ", expecting (<moduleName>):<beanName>:<socketName> OR <moduleName>:<beanName> with valid Java identifiers");
				return null;
			}
		}
	
		
		if(beanSocketQName != null) {
			// Bean Socket
			// We already check this socket exist in the provided list
			return new ModuleBeanSocketWireInfo(this.processingEnvironment, this.moduleElement, annotation, beanQNames, beanSocketQName);
		}
		else {
			// Module Socket
			if(!this.requiredModuleSockets.containsKey(moduleSocketQName)) {
				Arrays.stream(beanQNames)
					.filter(beanQName -> !this.beans.containsKey(beanQName))
					.forEach(beanQName -> wireReporter.error("There's no bean named " + beanQName));
				
				wireReporter.error("There's no socket named: " + moduleSocketQName);
				return null;
			}
			return new SocketBeanWireInfo(this.processingEnvironment, this.moduleElement, annotation, beanQNames, moduleSocketQName);
		}
	}
}
