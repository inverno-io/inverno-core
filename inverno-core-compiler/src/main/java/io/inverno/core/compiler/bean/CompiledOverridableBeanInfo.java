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

import javax.lang.model.element.ExecutableElement;
import javax.lang.model.type.TypeMirror;

import io.inverno.core.annotation.Bean.Strategy;
import io.inverno.core.annotation.Bean.Visibility;
import io.inverno.core.compiler.spi.BeanQualifiedName;
import io.inverno.core.compiler.spi.ModuleBeanInfo;
import io.inverno.core.compiler.spi.ModuleBeanSocketInfo;
import io.inverno.core.compiler.spi.NestedBeanInfo;
import io.inverno.core.compiler.spi.OverridableBeanInfo;
import io.inverno.core.compiler.spi.OverridingSocketBeanInfo;

/**
 * <p>
 * Represents wrapper bean info. A wrapper bean is necessarily compiled because a binary module only exposes module beans, the wrapper bean being hidden in the module implementation.
 * </p>
 * 
 * @author <a href="mailto:jeremy.kuhn@inverno.io">Jeremy Kuhn</a>
 *
 */
class CompiledOverridableBeanInfo implements OverridableBeanInfo {

	private ModuleBeanInfo overridableBeanInfo;
	
	private OverridingSocketBeanInfo overridingSocketInfo;
	
	public CompiledOverridableBeanInfo(
			ModuleBeanInfo overridableBeanInfo,
			OverridingSocketBeanInfo overridingSocketInfo) {
		
		this.overridableBeanInfo = overridableBeanInfo;
		this.overridingSocketInfo = overridingSocketInfo;
	}

	@Override
	public NestedBeanInfo[] getNestedBeans() {
		return this.overridableBeanInfo.getNestedBeans();
	}

	@Override
	public ModuleBeanInfo getOverridableBean() {
		return this.overridableBeanInfo;
	}

	@Override
	public OverridingSocketBeanInfo getOverridingSocket() {
		return this.overridingSocketInfo;
	}

	@Override
	public TypeMirror getProvidedType() {
		return this.overridableBeanInfo.getProvidedType();
	}

	@Override
	public Strategy getStrategy() {
		return this.overridableBeanInfo.getStrategy();
	}

	@Override
	public Visibility getVisibility() {
		return this.overridableBeanInfo.getVisibility();
	}

	@Override
	public ExecutableElement[] getInitElements() {
		return this.overridableBeanInfo.getInitElements();
	}

	@Override
	public ExecutableElement[] getDestroyElements() {
		return this.overridableBeanInfo.getDestroyElements();
	}

	@Override
	public ModuleBeanSocketInfo[] getSockets() {
		return this.overridableBeanInfo.getSockets();
	}

	@Override
	public ModuleBeanSocketInfo[] getRequiredSockets() {
		return this.overridableBeanInfo.getRequiredSockets();
	}

	@Override
	public ModuleBeanSocketInfo[] getOptionalSockets() {
		return this.overridableBeanInfo.getOptionalSockets();
	}

	@Override
	public BeanQualifiedName getQualifiedName() {
		return this.overridableBeanInfo.getQualifiedName();
	}

	@Override
	public TypeMirror getType() {
		return this.overridableBeanInfo.getType();
	}

	@Override
	public boolean hasError() {
		return this.overridableBeanInfo.hasError();
	}

	@Override
	public boolean hasWarning() {
		return this.overridableBeanInfo.hasWarning();
	}

	@Override
	public void error(String message) {
		this.overridableBeanInfo.error(message);
	}

	@Override
	public void warning(String message) {
		this.overridableBeanInfo.warning(message);
	}
}
