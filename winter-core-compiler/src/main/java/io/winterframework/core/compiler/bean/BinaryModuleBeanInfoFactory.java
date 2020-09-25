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
package io.winterframework.core.compiler.bean;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.ModuleElement;
import javax.lang.model.element.TypeElement;

import io.winterframework.core.compiler.WinterCompiler;
import io.winterframework.core.compiler.spi.BeanQualifiedName;
import io.winterframework.core.compiler.spi.ModuleBeanInfo;
import io.winterframework.core.compiler.spi.ModuleBeanSocketInfo;
import io.winterframework.core.compiler.spi.SocketBeanInfo;

/**
 * <p>
 * A {@link ModuleBeanInfoFactory} implementation used by the
 * {@link WinterCompiler} to create {@link ModuleBeanInfo} for binary
 * modules (ie. already compiled) required as component modules in other modules
 * (possibly compiled modules).
 * </p>
 * 
 * @author jkuhn
 *
 */
class BinaryModuleBeanInfoFactory extends ModuleBeanInfoFactory {

	private ModuleElement compiledModuleElement;
	
	private Map<BeanQualifiedName, List<SocketBeanInfo>> moduleSocketInfosByWiredBeanQName;
	
	/**
	 * @param processingEnvironment
	 * @param moduleElement
	 */
	public BinaryModuleBeanInfoFactory(ProcessingEnvironment processingEnvironment, ModuleElement moduleElement, ModuleElement compiledModuleElement, Supplier<List<? extends SocketBeanInfo>> moduleSocketInfosSupplier) {
		super(processingEnvironment, moduleElement);
		
		this.compiledModuleElement = compiledModuleElement;
		
		this.moduleSocketInfosByWiredBeanQName = new HashMap<>();
		
		Optional.ofNullable(moduleSocketInfosSupplier.get()).ifPresent(moduleSocketInfos -> {
			for(SocketBeanInfo moduleSocketInfo : moduleSocketInfos) {
				for(BeanQualifiedName wiredBeanQName : moduleSocketInfo.getWiredBeans()) {
					if(this.moduleSocketInfosByWiredBeanQName.get(wiredBeanQName) == null) {
						this.moduleSocketInfosByWiredBeanQName.put(wiredBeanQName, new ArrayList<>());
					}
					this.moduleSocketInfosByWiredBeanQName.get(wiredBeanQName).add(moduleSocketInfo);
				}
			}
		});
	}

	@Override
	public ModuleBeanInfo createBean(Element element) throws BeanCompilationException {
		if(!element.getKind().equals(ElementKind.METHOD)) {
			throw new IllegalArgumentException("Element must be a Method");
		}
		
		ExecutableElement executableElement = (ExecutableElement)element;
		if(!((TypeElement)executableElement.getEnclosingElement()).getQualifiedName().toString().equals(this.moduleQName.getClassName())) {
			throw new IllegalArgumentException("The specified element doesn't belong to module " + this.moduleQName);
		}
		if(!executableElement.getModifiers().contains(Modifier.PUBLIC) || executableElement.getParameters().size() != 0) {
			throw new IllegalArgumentException("Module bean " + new BeanQualifiedName(this.moduleQName, executableElement.getSimpleName().toString()) + " must be referenced as a public no-argument method");
		}
		
		BeanQualifiedName beanQName = new BeanQualifiedName(this.moduleQName, executableElement.getSimpleName().toString());
		ModuleBeanSocketInfoFactory beanSocketInfoFactory = ModuleBeanSocketInfoFactory.create(this.processingEnvironment, this.moduleElement, beanQName);
		
		List<? extends ModuleBeanSocketInfo> beanSocketInfos = null;
		if(this.moduleSocketInfosByWiredBeanQName.containsKey(beanQName)) {
			beanSocketInfos = this.moduleSocketInfosByWiredBeanQName.get(beanQName).stream()
				.map(moduleSocketInfo -> {
					CommonModuleBeanSingleSocketInfo beanSingleSocketInfo = (CommonModuleBeanSingleSocketInfo)beanSocketInfoFactory.createBeanSocket(beanQName, moduleSocketInfo);
					beanSingleSocketInfo.setBean(moduleSocketInfo);
					return beanSingleSocketInfo;
				})
				.collect(Collectors.toList());
		}
		// Use compiledModuleElement instead of moduleElement to report compilation errors on the compiled module
		return new CommonModuleBeanInfo(this.processingEnvironment, this.compiledModuleElement, null, beanQName, executableElement.getReturnType(), null, beanSocketInfos);
	}
}
