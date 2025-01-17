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

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import io.inverno.core.compiler.socket.WirableSocketBeanInfo;
import io.inverno.core.compiler.spi.BeanInfo;
import io.inverno.core.compiler.spi.BeanQualifiedName;
import io.inverno.core.compiler.spi.ModuleBeanInfo;
import io.inverno.core.compiler.spi.ModuleBeanMultiSocketInfo;
import io.inverno.core.compiler.spi.ModuleBeanSingleSocketInfo;
import io.inverno.core.compiler.spi.ModuleBeanSocketInfo;
import io.inverno.core.compiler.spi.ModuleInfo;
import io.inverno.core.compiler.spi.ModuleInfoVisitor;
import io.inverno.core.compiler.spi.ModuleQualifiedName;
import io.inverno.core.compiler.spi.MultiSocketBeanInfo;
import io.inverno.core.compiler.spi.MultiSocketInfo;
import io.inverno.core.compiler.spi.MutatorBeanInfo;
import io.inverno.core.compiler.spi.NestedBeanInfo;
import io.inverno.core.compiler.spi.OverridableBeanInfo;
import io.inverno.core.compiler.spi.OverridingSocketBeanInfo;
import io.inverno.core.compiler.spi.SingleSocketBeanInfo;
import io.inverno.core.compiler.spi.SingleSocketInfo;
import io.inverno.core.compiler.spi.SocketBeanInfo;
import io.inverno.core.compiler.spi.SocketInfo;
import io.inverno.core.compiler.spi.WrapperBeanInfo;

/**
 * <p>
 * Traverses the bean graph of a module and populates module's socket bean info with the direct and indirect beans they are wired to in order to detect dependency cycles when a module is used as a
 * component module
 * </p>
 * 
 * @author <a href="mailto:jeremy.kuhn@inverno.io">Jeremy Kuhn</a>
 *
 */
class ModuleBeanSocketWireResolver implements ModuleInfoVisitor<Void, Set<BeanQualifiedName>> {

	private ModuleQualifiedName moduleQName;
	
	@Override
	public Void visit(ModuleInfo moduleInfo, Set<BeanQualifiedName> wiredBeans) {
		this.moduleQName = moduleInfo.getQualifiedName();
		Arrays.stream(moduleInfo.getBeans()).forEach(bean -> {
			this.visit(bean, new HashSet<>());
		});
		return null;
	}

	@Override
	public Void visit(BeanInfo beanInfo, Set<BeanQualifiedName> wiredBeans) {
		// Optional unresolved sockets
		if(beanInfo == null) {
			return null;
		}
		else if(NestedBeanInfo.class.isAssignableFrom(beanInfo.getClass())) {
			return this.visit((NestedBeanInfo)beanInfo, wiredBeans);
		}
		else if(OverridableBeanInfo.class.isAssignableFrom(beanInfo.getClass())) {
			return this.visit((OverridableBeanInfo)beanInfo, wiredBeans);
		}
		else if(MutatorBeanInfo.class.isAssignableFrom(beanInfo.getClass())) {
			return this.visit((MutatorBeanInfo)beanInfo, wiredBeans);
		}
		else if(ModuleBeanInfo.class.isAssignableFrom(beanInfo.getClass())) {
			return this.visit((ModuleBeanInfo)beanInfo, wiredBeans);
		}
		else if(SocketBeanInfo.class.isAssignableFrom(beanInfo.getClass())) {
			return this.visit((SocketBeanInfo)beanInfo, wiredBeans);
		}
		return null;
	}
	
	@Override
	public Void visit(NestedBeanInfo nestedBeanInfo, Set<BeanQualifiedName> wiredBeans) {
		return this.visit(nestedBeanInfo.getProvidingBean(), wiredBeans);
	}

	@Override
	public Void visit(ModuleBeanInfo moduleBeanInfo, Set<BeanQualifiedName> wiredBeans) {
		wiredBeans.add(moduleBeanInfo.getQualifiedName());
		Arrays.stream(moduleBeanInfo.getSockets()).forEach(socket -> this.visit(socket, wiredBeans));
		
		return null;
	}

	@Override
	public Void visit(WrapperBeanInfo wrapperBeanInfo, Set<BeanQualifiedName> wiredBeans) {
		return this.visit((ModuleBeanInfo)wrapperBeanInfo, wiredBeans);
	}
	
	@Override
	public Void visit(OverridableBeanInfo overridableBeanInfo, Set<BeanQualifiedName> wiredBeans) {
		this.visit(overridableBeanInfo.getOverridingSocket(), wiredBeans);
		this.visit((ModuleBeanInfo)overridableBeanInfo, wiredBeans);
		return null;
	}

	@Override
	public Void visit(MutatorBeanInfo mutatorBeanInfo, Set<BeanQualifiedName> wiredBeans) {
		this.visit(mutatorBeanInfo.getMutatingSocket(), wiredBeans);
		this.visit((ModuleBeanInfo)mutatorBeanInfo, wiredBeans);
		return null;
	}

	@Override
	public Void visit(ModuleBeanSocketInfo beanSocketInfo, Set<BeanQualifiedName> wiredBeans) {
		if(ModuleBeanSingleSocketInfo.class.isAssignableFrom(beanSocketInfo.getClass())) {
			return this.visit((ModuleBeanSingleSocketInfo)beanSocketInfo, wiredBeans);
		}
		else if(ModuleBeanMultiSocketInfo.class.isAssignableFrom(beanSocketInfo.getClass())) {
			return this.visit((ModuleBeanMultiSocketInfo)beanSocketInfo, wiredBeans);
		}
		return null;
	}

	@Override
	public Void visit(ModuleBeanSingleSocketInfo beanSingleSocketInfo, Set<BeanQualifiedName> wiredBeans) {
		if(beanSingleSocketInfo.getBean() != null) {
			this.visit(beanSingleSocketInfo.getBean(), wiredBeans);
		}
		return null;
	}

	@Override
	public Void visit(ModuleBeanMultiSocketInfo beanMultiSocketInfo, Set<BeanQualifiedName> wiredBeans) {
		if(beanMultiSocketInfo.getBeans() != null) {
			Arrays.stream(beanMultiSocketInfo.getBeans()).forEach(bean -> this.visit(bean, wiredBeans));
		}
		return null;
	}

	@Override
	public Void visit(SocketBeanInfo moduleSocketInfo, Set<BeanQualifiedName> wiredBeans) {
		if(moduleSocketInfo.getQualifiedName().getModuleQName().equals(this.moduleQName)) {
			// this module
			wiredBeans.addAll(Arrays.asList(moduleSocketInfo.getWiredBeans()));
			((WirableSocketBeanInfo)moduleSocketInfo).setWiredBeans(wiredBeans.stream().filter(beanQName -> beanQName.getModuleQName().equals(this.moduleQName)).collect(Collectors.toSet()));
		}
		else {
			// component module
			if(SingleSocketBeanInfo.class.isAssignableFrom(moduleSocketInfo.getClass())) {
				return this.visit((SingleSocketBeanInfo)moduleSocketInfo, wiredBeans);
			}
			else if(MultiSocketBeanInfo.class.isAssignableFrom(moduleSocketInfo.getClass())) {
				return this.visit((MultiSocketBeanInfo)moduleSocketInfo, wiredBeans);
			}
		}
		return null;
	}

	@Override
	public Void visit(SingleSocketBeanInfo moduleSingleSocketInfo, Set<BeanQualifiedName> wiredBeans) {
		if(moduleSingleSocketInfo.getBean() != null) {
			/*wiredBeans.addAll(Arrays.stream(moduleSingleSocketInfo.getWiredBeans())
				.filter(beanQName -> beanQName.getModuleQName().equals(this.moduleQName))
				.collect(Collectors.toList()));*/
			this.visit(moduleSingleSocketInfo.getBean(), wiredBeans);
		}
		return null;
	}

	@Override
	public Void visit(MultiSocketBeanInfo moduleMultiSocketInfo, Set<BeanQualifiedName> wiredBeans) {
		if(moduleMultiSocketInfo.getBeans() != null) {
			/*wiredBeans.addAll(Arrays.stream(moduleMultiSocketInfo.getWiredBeans())
				.filter(beanQName -> beanQName.getModuleQName().equals(this.moduleQName))
				.collect(Collectors.toList()));*/
			Arrays.stream(moduleMultiSocketInfo.getBeans()).forEach(bean -> this.visit(bean, wiredBeans));
		}
		return null;
	}
	
	@Override
	public Void visit(OverridingSocketBeanInfo overridingSocketBeanInfo, Set<BeanQualifiedName> wiredBeans) {
		return this.visit((SocketBeanInfo)overridingSocketBeanInfo, wiredBeans);
	}
	
	@Override
	public Void visit(SocketInfo socketInfo, Set<BeanQualifiedName> wiredBeans) {
		return null;
	}

	@Override
	public Void visit(SingleSocketInfo singleSocketInfo, Set<BeanQualifiedName> wiredBeans) {
		return null;
	}

	@Override
	public Void visit(MultiSocketInfo multiSocketInfo, Set<BeanQualifiedName> wiredBeans) {
		return null;
	}
}
