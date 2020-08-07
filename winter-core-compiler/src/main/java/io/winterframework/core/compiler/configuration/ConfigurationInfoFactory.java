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
package io.winterframework.core.compiler.configuration;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.ModuleElement;

import io.winterframework.core.compiler.ModuleAnnotationProcessor;
import io.winterframework.core.compiler.TypeErrorException;
import io.winterframework.core.compiler.bean.BeanCompilationException;
import io.winterframework.core.compiler.common.AbstractInfoFactory;
import io.winterframework.core.compiler.spi.ConfigurationInfo;

/**
 * <p>
 * Used by the {@link ModuleAnnotationProcessor} to create
 * {@link ConfigurationInfoFactory} corresponding to a particular context
 * (compiled and binary).
 * </p>
 * 
 * <p>
 * Note that in the case of {@link ConfigurationInfo}, only compiled modules is
 * to be considered.
 * </p>
 * 
 * @author jkuhn
 *
 */
public abstract class ConfigurationInfoFactory extends AbstractInfoFactory {
	
	protected ConfigurationInfoFactory(ProcessingEnvironment processingEnvironment, ModuleElement moduleElement) {
		super(processingEnvironment, moduleElement);
	}
	
	public static ConfigurationInfoFactory create(ProcessingEnvironment processingEnvironment, ModuleElement moduleElement) {
		return new CompiledConfigurationInfoFactory(processingEnvironment, moduleElement);
	}
	
	public abstract ConfigurationInfo createConfiguration(Element element) throws BeanCompilationException, TypeErrorException;
}
