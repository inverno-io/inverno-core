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

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Element;

import io.winterframework.core.annotation.Bean;
import io.winterframework.core.compiler.common.AbstractInfo;
import io.winterframework.core.compiler.spi.ModuleBeanInfo;
import io.winterframework.core.compiler.spi.ModuleInfo;
import io.winterframework.core.compiler.spi.ModuleInfoVisitor;
import io.winterframework.core.compiler.spi.ModuleQualifiedName;
import io.winterframework.core.compiler.spi.SocketBeanInfo;

/**
 * <p>
 * Represents module info of a binary module required and included in other
 * modules (possibly compiled modules).
 * </p>
 * 
 * @author jkuhn
 *
 */
class BinaryModuleInfo extends AbstractInfo<ModuleQualifiedName> implements ModuleInfo {

	private int version;
	
	private boolean faulty;
	
	private List<SocketBeanInfo> socketInfos;
	
	private List<ModuleBeanInfo> beanInfos;
	
	public BinaryModuleInfo(ProcessingEnvironment processingEnvironment, Element element, ModuleQualifiedName qname, int version, List<ModuleBeanInfo> beanInfos, List<SocketBeanInfo> socketInfos) {
		super(processingEnvironment, element, qname);
		
		this.version = version;
		
		if(beanInfos.stream().anyMatch(beanInfo -> !beanInfo.getVisibility().equals(Bean.Visibility.PUBLIC))) {
			throw new IllegalArgumentException("Only public beans can be injected to a required module");
		}
		this.beanInfos = beanInfos != null ? Collections.unmodifiableList(beanInfos) : Collections.emptyList();
		this.socketInfos = socketInfos != null ? Collections.unmodifiableList(socketInfos) : Collections.emptyList();
	}

	void setFaulty(boolean faulty) {
		this.faulty = faulty;
	}

	@Override
	public int getVersion() {
		return this.version;
	}
	
	@Override
	public boolean isFaulty() {
		return this.faulty;
	}
	
	@Override
	public ModuleQualifiedName getQualifiedName() {
		return this.qname;
	}
	
	@Override
	public ModuleInfo[] getModules() {
		return new ModuleInfo[0];
	}

	@Override
	public SocketBeanInfo[] getSockets() {
		return this.socketInfos.stream().toArray(SocketBeanInfo[]::new);
	}

	@Override
	public Optional<ModuleBeanInfo> getBean(String name) {
		return this.beanInfos.stream().filter(bean -> bean.getQualifiedName().getSimpleValue().equals(name)).findFirst();
	}

	@Override
	public ModuleBeanInfo[] getBeans() {
		return this.beanInfos.stream().toArray(ModuleBeanInfo[]::new);
	}

	@Override
	public ModuleBeanInfo[] getPrivateBeans() {
		return new ModuleBeanInfo[0];
	}

	@Override
	public ModuleBeanInfo[] getPublicBeans() {
		return this.getBeans();
	}

	@Override
	public <R, P> R accept(ModuleInfoVisitor<R, P> visitor, P p) {
		return visitor.visit(this,  p);
	}
}
