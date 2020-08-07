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

import java.util.Arrays;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.ModuleElement;

import io.winterframework.core.annotation.Bean;
import io.winterframework.core.compiler.spi.ConfigurationInfo;
import io.winterframework.core.compiler.spi.ModuleBeanInfo;
import io.winterframework.core.compiler.spi.ModuleInfo;
import io.winterframework.core.compiler.spi.ModuleInfoBuilder;
import io.winterframework.core.compiler.spi.ModuleQualifiedName;
import io.winterframework.core.compiler.spi.SocketBeanInfo;

/**
 * <p>
 * A module info builder used to build binary Winter module info from module
 * elements annotated with {@link Module}, required and included in other
 * modules (possibly compiled modules).
 * </p>
 * 
 * @author jkuhn
 *
 */
class BinaryModuleInfoBuilder extends AbstractModuleInfoBuilder {

	private ModuleBeanInfo[] beans;
	
	private SocketBeanInfo[] sockets;
	
	public BinaryModuleInfoBuilder(ProcessingEnvironment processingEnvironment, ModuleElement moduleElement) {
		super(processingEnvironment, moduleElement);
		
		this.beans = new ModuleBeanInfo[0];
		this.sockets = new SocketBeanInfo[0];
	}
	
	@Override
	public ModuleQualifiedName getQualifiedName() {
		return this.moduleQName;
	}
	
	@Override
	public ModuleInfoBuilder beans(ModuleBeanInfo[] beans) {
		if(beans == null) {
			this.beans = new ModuleBeanInfo[0];
		}
		else {
			if(Arrays.stream(beans).anyMatch(beanInfo -> !beanInfo.getVisibility().equals(Bean.Visibility.PUBLIC))) {
				throw new IllegalArgumentException("Only public beans can be injected into a component module");
			}
			this.beans = beans;
		}
		return this;
	}

	@Override
	public ModuleInfoBuilder sockets(SocketBeanInfo[] sockets) {
		this.sockets = sockets != null ? sockets : new SocketBeanInfo[0];
		return this;
	}

	@Override
	public ModuleInfoBuilder configurations(ConfigurationInfo[] configurations) {
		throw new UnsupportedOperationException("You can't inject configuration beans into a component module");
	}
	
	@Override
	public ModuleInfoBuilder modules(ModuleInfo[] modules) {
		throw new UnsupportedOperationException("You can't inject modules into a component module");
	}

	@Override
	public ModuleInfo build() {
		return new BinaryModuleInfo(this.processingEnvironment, this.moduleElement, this.moduleQName, this.version, Arrays.asList(this.beans), Arrays.asList(this.sockets));
	}
}
