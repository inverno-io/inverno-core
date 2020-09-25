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
package io.winterframework.core.compiler.module;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.ModuleElement;

import io.winterframework.core.compiler.WinterCompiler;
import io.winterframework.core.compiler.spi.ModuleBeanInfo;
import io.winterframework.core.compiler.spi.ModuleInfo;
import io.winterframework.core.compiler.spi.ModuleInfoBuilder;
import io.winterframework.core.compiler.spi.ModuleQualifiedName;
import io.winterframework.core.compiler.spi.SocketBeanInfo;

/**
 * <p>
 * Extracts qualified name and version of a module element. The version
 * corresponds to the current {@link WinterCompiler#VERSION} of the
 * version of a binary module determined by the version of its Module class.
 * </p>
 * 
 * @author jkuhn
 *
 */
public class ModuleMetadataExtractor {

	private MetadataModuleInfoBuilder versionModuleInfoBuilder;
	
	private class MetadataModuleInfoBuilder extends AbstractModuleInfoBuilder {

		public MetadataModuleInfoBuilder(ProcessingEnvironment processingEnvironment, ModuleElement moduleElement) {
			super(processingEnvironment, moduleElement);
		}
		
		@Override
		public ModuleQualifiedName getQualifiedName() {
			return this.moduleQName;
		}

		@Override
		public ModuleInfoBuilder beans(ModuleBeanInfo[] beans) {
			throw new UnsupportedOperationException();
		}

		@Override
		public ModuleInfoBuilder sockets(SocketBeanInfo[] sockets) {
			throw new UnsupportedOperationException();
		}

		@Override
		public ModuleInfoBuilder modules(ModuleInfo[] modules) {
			throw new UnsupportedOperationException();
		}

		@Override
		public ModuleInfo build() {
			throw new UnsupportedOperationException();
		}
	}
	
	public ModuleMetadataExtractor(ProcessingEnvironment processingEnvironment, ModuleElement moduleElement) {
		this.versionModuleInfoBuilder = new MetadataModuleInfoBuilder(processingEnvironment, moduleElement);
	}

	public Integer getModuleVersion() {
		return this.versionModuleInfoBuilder.version;
	}
	
	public ModuleQualifiedName getModuleQualifiedName() {
		return this.versionModuleInfoBuilder.moduleQName;
	}
}
