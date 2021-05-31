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
package io.inverno.core.compiler.bean;

import java.util.List;
import java.util.function.Supplier;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.ModuleElement;

import io.inverno.core.compiler.InvernoCompiler;
import io.inverno.core.compiler.common.AbstractInfoFactory;
import io.inverno.core.compiler.spi.ModuleBeanInfo;
import io.inverno.core.compiler.spi.SocketBeanInfo;

/**
 * <p>
 * Used by the {@link InvernoCompiler} to create
 * {@link ModuleBeanInfoFactory} corresponding to a particular context (compiled
 * and binary).
 * </p>
 * 
 * @author <a href="mailto:jeremy.kuhn@inverno.io">Jeremy Kuhn</a>
 *
 */
public abstract class ModuleBeanInfoFactory extends AbstractInfoFactory {

	protected ModuleBeanInfoFactory(ProcessingEnvironment processingEnvironment, ModuleElement moduleElement) {
		super(processingEnvironment, moduleElement);
	}

	public static ModuleBeanInfoFactory create(ProcessingEnvironment processingEnvironment, ModuleElement moduleElement) {
		return new CompiledModuleBeanInfoFactory(processingEnvironment, moduleElement);
	}
	
	public static ModuleBeanInfoFactory create(ProcessingEnvironment processingEnvironment, ModuleElement moduleElement, ModuleElement requiredModuleElement, Supplier<List<? extends SocketBeanInfo>> moduleSocketInfosSupplier, Integer version) {
		if(moduleElement.getDirectives().stream().noneMatch(directive -> directive.getKind().equals(ModuleElement.DirectiveKind.REQUIRES) && ((ModuleElement.RequiresDirective)directive).getDependency().equals(requiredModuleElement))) {
			throw new IllegalArgumentException("The specified element is not required in module " + moduleElement.getQualifiedName().toString());
		}
		
		if(version == null) {
			throw new IllegalStateException("Version of binary module can't be null");			
		}
		switch(version) {
			case 1: return new BinaryModuleBeanInfoFactory(processingEnvironment, requiredModuleElement, moduleElement, moduleSocketInfosSupplier);
			default: throw new IllegalStateException("Unsupported version: " + version);
		}
	}
	
	public abstract ModuleBeanInfo createBean(Element element) throws BeanCompilationException;
}
