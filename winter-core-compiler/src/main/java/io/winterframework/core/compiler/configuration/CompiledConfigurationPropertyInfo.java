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
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.type.TypeMirror;

import io.winterframework.core.compiler.common.ReporterInfo;
import io.winterframework.core.compiler.spi.ConfigurationInfo;
import io.winterframework.core.compiler.spi.ConfigurationPropertyInfo;

/**
 * <p>
 * Represents a compiled {@link ConfigurationPropertyInfo} attached to a
 * {@link ConfigurationPropertyInfo} when a module is compiled.
 * </p>
 * 
 * @author jkuhn
 *
 */
public class CompiledConfigurationPropertyInfo extends ReporterInfo implements ConfigurationPropertyInfo {

	protected String name;
	
	protected ConfigurationInfo configurationInfo;
	
	protected TypeMirror type;
	
	protected boolean isDefault;
	
	public CompiledConfigurationPropertyInfo(ProcessingEnvironment processingEnvironment, ExecutableElement propertyMethod) {
		super(processingEnvironment, propertyMethod);
		this.name = propertyMethod.getSimpleName().toString();
		this.type = propertyMethod.getReturnType();
		this.isDefault = propertyMethod.isDefault();
	}
	
	public void setConfiguration(ConfigurationInfo configuration) {
		this.configurationInfo = configuration;
	}
	
	@Override
	public ConfigurationInfo getConfiguration() {
		return this.configurationInfo;
	}
	
	@Override
	public boolean isDefault() {
		return this.isDefault;
	}

	@Override
	public String getName() {
		return this.name;
	}

	@Override
	public TypeMirror getType() {
		return this.type;
	}
}
