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
import java.util.stream.Stream;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.ModuleElement;

import io.winterframework.core.annotation.Bean;
import io.winterframework.core.compiler.WinterCompiler;
import io.winterframework.core.compiler.common.AbstractInfo;
import io.winterframework.core.compiler.spi.ModuleBeanInfo;
import io.winterframework.core.compiler.spi.ModuleInfo;
import io.winterframework.core.compiler.spi.ModuleInfoVisitor;
import io.winterframework.core.compiler.spi.ModuleQualifiedName;
import io.winterframework.core.compiler.spi.OverridableBeanInfo;
import io.winterframework.core.compiler.spi.SocketBeanInfo;

/**
 * <p>
 * Represents module info of a compiled module. A compiled module is a module
 * that is currently compiled by the Java compiler and processed by the
 * {@link WinterCompiler}.
 * </p>
 * 
 * @author <a href="mailto:jeremy.kuhn@winterframework.io">Jeremy Kuhn</a>
 *
 */
class CompiledModuleInfo extends AbstractInfo<ModuleQualifiedName> implements ModuleInfo {

	private int version;
	
	private boolean faulty;
	
	private List<ModuleBeanInfo> beanInfos;
	
	private List<SocketBeanInfo> socketInfos;
	
	private List<ModuleInfo> moduleInfos;
	
	public CompiledModuleInfo(ProcessingEnvironment processingEnvironment, ModuleElement element, AnnotationMirror annotation, ModuleQualifiedName qname, int version, List<ModuleBeanInfo> beanInfos, List<SocketBeanInfo> socketInfos, List<ModuleInfo> moduleInfos) {
		super(processingEnvironment, element, annotation, qname);
		
		this.version = version;
		this.beanInfos = beanInfos != null ? Collections.unmodifiableList(beanInfos) : Collections.emptyList();
		this.socketInfos = socketInfos != null ? Collections.unmodifiableList(socketInfos) : Collections.emptyList();
		this.moduleInfos = moduleInfos != null ? Collections.unmodifiableList(moduleInfos) : Collections.emptyList();
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
		return this.moduleInfos.stream().toArray(ModuleInfo[]::new);
	}

	@Override
	public SocketBeanInfo[] getSockets() {
		return Stream.concat(this.socketInfos.stream(), this.beanInfos.stream().filter(beanInfo -> beanInfo instanceof OverridableBeanInfo).map(beanInfo -> ((OverridableBeanInfo)beanInfo).getOverridingSocket())).toArray(SocketBeanInfo[]::new);
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
		return this.beanInfos.stream().filter(beanInfo -> beanInfo.getVisibility().equals(Bean.Visibility.PRIVATE)).toArray(ModuleBeanInfo[]::new);
	}

	@Override
	public ModuleBeanInfo[] getPublicBeans() {
		return this.beanInfos.stream().filter(beanInfo -> beanInfo.getVisibility().equals(Bean.Visibility.PUBLIC)).toArray(ModuleBeanInfo[]::new);
	}

	@Override
	public <R, P> R accept(ModuleInfoVisitor<R, P> visitor, P p) {
		return visitor.visit(this,  p);
	}
	
	@Override
	public ModuleElement getElement() {
		return (ModuleElement)super.getElement();
	}
}
