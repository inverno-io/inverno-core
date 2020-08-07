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

import java.util.List;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.type.TypeMirror;

import io.winterframework.core.compiler.common.AbstractInfo;
import io.winterframework.core.compiler.spi.BeanQualifiedName;
import io.winterframework.core.compiler.spi.ConfigurationInfo;
import io.winterframework.core.compiler.spi.ConfigurationPropertyInfo;
import io.winterframework.core.compiler.spi.ConfigurationSocketBeanInfo;
import io.winterframework.core.compiler.spi.NestedConfigurationPropertyInfo;

/**
 * <p>
 * Represents the compiled {@link ConfigurationInfo} created for a configuration
 * bean when a module is compiled. A configuration bean is necessarily compiled
 * because a binary module only needs to provide the corresponding configuration
 * socket.
 * </p>
 * 
 * @author jkuhn
 *
 */
public class CompiledConfigurationInfo extends AbstractInfo<BeanQualifiedName> implements ConfigurationInfo {

	private TypeMirror type;
	
	private List<? extends ConfigurationPropertyInfo> configurationProperties;
	
	private ConfigurationSocketBeanInfo socketBean;
	
	public CompiledConfigurationInfo(ProcessingEnvironment processingEnvironment, 
			Element element,
			AnnotationMirror annotation, 
			BeanQualifiedName qname,
			TypeMirror configurationType,
			List<? extends CompiledConfigurationPropertyInfo> configurationProperties) {
		super(processingEnvironment, element, annotation, qname);
		
		this.type = configurationType;
		this.socketBean = new CompiledConfigurationSocketBeanInfo(this.processingEnvironment, element, annotation, qname, type);
		
		configurationProperties.stream().forEach(configurationProperty -> configurationProperty.setConfiguration(this));
		this.configurationProperties = configurationProperties;
	}

	@Override
	public TypeMirror getType() {
		return this.type;
	}

	@Override
	public ConfigurationPropertyInfo[] getProperties() {
		return this.configurationProperties.stream().toArray(ConfigurationPropertyInfo[]::new);
	}
	
	@Override
	public NestedConfigurationPropertyInfo[] getSimpleProperties() {
		return this.configurationProperties.stream().filter(property -> !(property instanceof NestedConfigurationPropertyInfo)).toArray(NestedConfigurationPropertyInfo[]::new);
	}
	
	@Override
	public NestedConfigurationPropertyInfo[] getNestedConfigurationProperties() {
		return this.configurationProperties.stream().filter(property -> property instanceof NestedConfigurationPropertyInfo).toArray(NestedConfigurationPropertyInfo[]::new);
	}

	@Override
	public ConfigurationSocketBeanInfo getSocket() {
		return this.socketBean;
	}
}
