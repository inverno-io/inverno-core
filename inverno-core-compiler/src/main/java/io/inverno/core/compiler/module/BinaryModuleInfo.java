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
package io.inverno.core.compiler.module;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.ModuleElement;

import io.inverno.core.annotation.Bean;
import io.inverno.core.compiler.common.AbstractInfo;
import io.inverno.core.compiler.spi.ModuleBeanInfo;
import io.inverno.core.compiler.spi.ModuleInfo;
import io.inverno.core.compiler.spi.ModuleInfoVisitor;
import io.inverno.core.compiler.spi.ModuleQualifiedName;
import io.inverno.core.compiler.spi.SocketBeanInfo;

/**
 * <p>
 * Represents module info of a binary module required and included in other modules (possibly compiled modules).
 * </p>
 * 
 * @author <a href="mailto:jeremy.kuhn@inverno.io">Jeremy Kuhn</a>
 *
 */
class BinaryModuleInfo extends AbstractInfo<ModuleQualifiedName> implements ModuleInfo {
	
	private final int version;
	
	private final List<SocketBeanInfo> socketInfos;
	
	private List<ModuleBeanInfo> beanInfos;
	
	private boolean faulty;
	
	public BinaryModuleInfo(ProcessingEnvironment processingEnvironment, ModuleElement element, ModuleQualifiedName qname, int version, List<ModuleBeanInfo> beanInfos, List<SocketBeanInfo> socketInfos) {
		super(processingEnvironment, element, qname);
		
		this.version = version;
		
		if(beanInfos != null) {
			if(beanInfos.stream().anyMatch(beanInfo -> !beanInfo.getVisibility().equals(Bean.Visibility.PUBLIC))) {
				throw new IllegalArgumentException("Only public beans can be injected to a required module");
			}
			this.beanInfos = Collections.unmodifiableList(beanInfos);
		}
		else {
			this.beanInfos = Collections.emptyList();
		}
		
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
	
	@Override
	public ModuleElement getElement() {
		return (ModuleElement)super.getElement();
	}

	/**
	 * <p>
	 * A binary module is never empty since a binary module is created from a module class which is only generated when the module has beans or component modules.
	 * </p>
	 */
	@Override
	public boolean isEmpty() {
		
		return false;
	}
}
