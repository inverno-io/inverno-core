/*
 * Copyright 2020 Jeremy KUHN
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
package io.winterframework.core.compiler.configuration;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.ModuleElement;

import io.winterframework.core.compiler.module.ModuleMetadataExtractor;
import io.winterframework.core.compiler.spi.BeanQualifiedName;
import io.winterframework.core.compiler.spi.ConfigurationPropertyInfo;
import io.winterframework.core.compiler.spi.NestedConfigurationPropertyInfo;

/**
 * <p>
 * Represents the compiled {@link NestedConfigurationPropertyInfo} attached to a
 * {@link ConfigurationPropertyInfo} when a module is compiled.
 * </p>
 * 
 * @author jkuhn
 *
 */
public class CompiledNestedConfigurationPropertyInfo extends CompiledConfigurationPropertyInfo implements NestedConfigurationPropertyInfo {

	private BeanQualifiedName qname;
	
	private String builderClassName;
	
	public CompiledNestedConfigurationPropertyInfo(
			ProcessingEnvironment processingEnvironment, 
			ExecutableElement propertyMethod,
			BeanQualifiedName configurationQName) {
		super(processingEnvironment, propertyMethod);
		
		this.qname = new BeanQualifiedName(configurationQName.getModuleQName(), configurationQName.getBeanName() + "." + this.getName());
		
		ModuleMetadataExtractor moduleMetadataExtractor = new ModuleMetadataExtractor(this.processingEnvironment, this.getEnclosingModuleElement(this.processingEnvironment.getTypeUtils().asElement(this.type)));
		this.builderClassName = moduleMetadataExtractor.getModuleQualifiedName().getClassName() + "." + this.processingEnvironment.getTypeUtils().asElement(this.type).getSimpleName().toString() + "Builder";
	}
	
	private ModuleElement getEnclosingModuleElement(Element element) {
		if(element != null) {
			if(element.getKind().equals(ElementKind.MODULE)) {
				return (ModuleElement)element;
			}
			return this.getEnclosingModuleElement(element.getEnclosingElement());
		}
		return null;
	}

	@Override
	public String getBuilderClassName() {
		return this.builderClassName;
	}

	@Override
	public BeanQualifiedName getQualifiedName() {
		return qname;
	}
}
