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
package io.winterframework.core.compiler.bean;

import javax.lang.model.type.TypeMirror;

import io.winterframework.core.compiler.spi.BeanInfo;
import io.winterframework.core.compiler.spi.BeanQualifiedName;
import io.winterframework.core.compiler.spi.ModuleBeanInfo;
import io.winterframework.core.compiler.spi.NestedBeanInfo;
import io.winterframework.core.compiler.spi.NestedConfigurationPropertyInfo;

/**
 * @author jkuhn
 *
 */
public class NestedConfigurationBeanInfo implements NestedBeanInfo {

	private BeanQualifiedName qname;
	
	private ModuleBeanInfo providingBeanInfo;
	
	private NestedConfigurationPropertyInfo nestedConfigurationPropertyInfo;
	
	public NestedConfigurationBeanInfo(ModuleBeanInfo providingBeanInfo, NestedConfigurationPropertyInfo nestedConfigurationPropertyInfo) {
		this.providingBeanInfo = providingBeanInfo;
		this.qname = new BeanQualifiedName(providingBeanInfo.getQualifiedName().getModuleQName(), providingBeanInfo.getQualifiedName().getBeanName() + "." + nestedConfigurationPropertyInfo.getName());
		this.nestedConfigurationPropertyInfo = nestedConfigurationPropertyInfo;
	}

	@Override
	public BeanQualifiedName getQualifiedName() {
		return this.qname;
	}

	@Override
	public TypeMirror getType() {
		return this.nestedConfigurationPropertyInfo.getType();
	}

	@Override
	public BeanInfo[] getNestedBeans() {
		return new BeanInfo[0];
	}

	@Override
	public boolean hasError() {
		return this.providingBeanInfo.hasError();
	}

	@Override
	public boolean hasWarning() {
		return this.providingBeanInfo.hasWarning();
	}

	@Override
	public void error(String message) {
		this.providingBeanInfo.error(message);
	}

	@Override
	public void warning(String message) {
		this.providingBeanInfo.warning(message);
	}

	@Override
	public BeanInfo getProvidingBean() {
		return this.providingBeanInfo;
	}
	
	@Override
	public String getName() {
		return this.nestedConfigurationPropertyInfo.getName();
	}
}
