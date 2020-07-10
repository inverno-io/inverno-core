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
package io.winterframework.core.compiler;

import java.util.Arrays;
import java.util.stream.Collectors;

import io.winterframework.core.compiler.spi.BeanInfo;
import io.winterframework.core.compiler.spi.ModuleBeanInfo;
import io.winterframework.core.compiler.spi.ModuleBeanMultiSocketInfo;
import io.winterframework.core.compiler.spi.ModuleBeanSingleSocketInfo;
import io.winterframework.core.compiler.spi.ModuleBeanSocketInfo;
import io.winterframework.core.compiler.spi.ModuleInfo;
import io.winterframework.core.compiler.spi.ModuleInfoVisitor;
import io.winterframework.core.compiler.spi.MultiSocketBeanInfo;
import io.winterframework.core.compiler.spi.MultiSocketInfo;
import io.winterframework.core.compiler.spi.MultiSocketType;
import io.winterframework.core.compiler.spi.SingleSocketBeanInfo;
import io.winterframework.core.compiler.spi.SingleSocketInfo;
import io.winterframework.core.compiler.spi.SocketBeanInfo;
import io.winterframework.core.compiler.spi.SocketInfo;
import io.winterframework.core.compiler.spi.WrapperBeanInfo;

/**
 * <p>A {@link ModuleInfoVisitor} used to generate module descriptor in a readable YAML format.</p>
 * 
 * @author jkuhn
 *
 */
class ModuleDescriptorGenerator implements ModuleInfoVisitor<String, String> {

	private String indent = DEFAULT_INDENT;
	
	private static final String DEFAULT_INDENT = "    ";
	
	public String getIndent() {
		return this.indent;
	}

	public void setIndent(String indent) {
		this.indent = indent;
	}

	@Override
	public String visit(ModuleInfo moduleInfo, String pad) {
		StringBuilder result = new StringBuilder();
		
		result.append(pad).append("module:").append("\n");
		result.append(pad).append(this.indent).append("name: ").append(moduleInfo.getQualifiedName().toString()).append("\n");
		result.append(pad).append(this.indent).append("class: ").append(moduleInfo.getQualifiedName().getClassName()).append("\n");
		result.append(pad).append(this.indent).append("modules:").append("\n");
		for(ModuleInfo module : moduleInfo.getModules()) {
			result.append(this.visit(module, pad + this.indent + this.indent)).append("\n");
		}
		result.append(pad).append(this.indent).append("sockets:").append("\n");
		for(SocketBeanInfo socket : moduleInfo.getSockets()) {
			result.append(this.visit(socket, pad + this.indent + this.indent)).append("\n");
		}
		
		result.append(pad).append(this.indent).append("beans:").append("\n");

		result.append(pad).append(this.indent).append(this.indent).append("private:").append("\n");
		for(ModuleBeanInfo bean : moduleInfo.getPrivateBeans()) {
			result.append(this.visit(bean, pad + this.indent + this.indent + this.indent)).append("\n");
		}
		
		result.append(pad).append(this.indent).append(this.indent).append("public:").append("\n");
		for(ModuleBeanInfo bean : moduleInfo.getPublicBeans()) {
			result.append(this.visit(bean, pad + this.indent + this.indent + this.indent)).append("\n");
		}
		return result.toString();
	}

	@Override
	public String visit(BeanInfo beanInfo, String pad) {
		if(ModuleBeanInfo.class.isAssignableFrom(beanInfo.getClass())) {
			return this.visit((ModuleBeanInfo)beanInfo, pad);
		}
		else if(SocketBeanInfo.class.isAssignableFrom(beanInfo.getClass())) {
			return this.visit((SocketBeanInfo)beanInfo, pad);
		}
		return "";
	}

	@Override
	public String visit(ModuleBeanInfo moduleBeanInfo, String pad) {
		StringBuilder result = new StringBuilder();
		
		result.append(pad).append("- ").append("name: ").append(moduleBeanInfo.getQualifiedName().getSimpleValue()).append("\n");
		result.append(pad).append("  ").append("type: ").append(moduleBeanInfo.getType().toString()).append("\n");
		if(moduleBeanInfo.getProvidedType() != null) {
			result.append(pad).append("  ").append("providedType: ").append(moduleBeanInfo.getProvidedType().toString()).append("\n");
		}
		result.append(pad).append("  ").append("strategy: ").append(moduleBeanInfo.getStrategy().toString()).append("\n");
		if(WrapperBeanInfo.class.isAssignableFrom(moduleBeanInfo.getClass())) {
			result.append(pad).append("  ").append("wrapperType:").append(((WrapperBeanInfo)moduleBeanInfo).getWrapperType().toString()).append("\n");
		}
		result.append(pad).append("  ").append("init: ").append("\n");
		if(moduleBeanInfo.getInitElements().length > 0) {
			result.append(Arrays.stream(moduleBeanInfo.getInitElements()).map(init -> pad +  "  " +  this.indent +  "- " + init.toString()).collect(Collectors.joining("\n"))).append("\n");
		}
		result.append(pad).append("  ").append("destroy:").append("\n");
		if(moduleBeanInfo.getDestroyElements().length > 0) {
			result.append(Arrays.stream(moduleBeanInfo.getDestroyElements()).map(destroy -> pad +  "  " +  this.indent +  "- " + destroy.toString()).collect(Collectors.joining("\n"))).append("\n");
		}
		result.append(pad).append("  ").append("sockets:");
		if(moduleBeanInfo.getSockets().length > 0) {
			result.append("\n");
			result.append(Arrays.stream(moduleBeanInfo.getSockets()).map(socket -> this.visit(socket, pad + "  " + this.indent)).collect(Collectors.joining("\n")));
		}
		return result.toString();
	}

	@Override
	public String visit(WrapperBeanInfo moduleWrapperBeanInfo, String pad) {
		return this.visit((ModuleBeanInfo)moduleWrapperBeanInfo, pad);
	}
	
	@Override
	public String visit(ModuleBeanSocketInfo beanSocketInfo, String pad) {
		if(ModuleBeanSingleSocketInfo.class.isAssignableFrom(beanSocketInfo.getClass())) {
			return this.visit((ModuleBeanSingleSocketInfo)beanSocketInfo, pad);
		}
		else if(ModuleBeanMultiSocketInfo.class.isAssignableFrom(beanSocketInfo.getClass())) {
			return this.visit((ModuleBeanMultiSocketInfo)beanSocketInfo, pad);
		}
		return "";
	}

	@Override
	public String visit(ModuleBeanSingleSocketInfo beanSingleSocketInfo, String pad) {
		return this.visit((SingleSocketInfo)beanSingleSocketInfo, pad);
	}

	@Override
	public String visit(ModuleBeanMultiSocketInfo beanMultiSocketInfo, String pad) {
		return this.visit((MultiSocketInfo)beanMultiSocketInfo, pad);
	}

	@Override
	public String visit(SocketBeanInfo moduleSocketInfo, String pad) {
		if(SingleSocketBeanInfo.class.isAssignableFrom(moduleSocketInfo.getClass())) {
			return this.visit((SingleSocketBeanInfo)moduleSocketInfo, pad);
		}
		else if(MultiSocketBeanInfo.class.isAssignableFrom(moduleSocketInfo.getClass())) {
			return this.visit((MultiSocketBeanInfo)moduleSocketInfo, pad);
		}
		return "";
	}

	@Override
	public String visit(SingleSocketBeanInfo moduleSingleSocketInfo, String pad) {
		StringBuilder result = new StringBuilder();
		
		result.append(this.visit((SingleSocketInfo)moduleSingleSocketInfo, pad)).append("\n");
		result.append(pad).append("  ").append("wiredTo:");
		if(moduleSingleSocketInfo.getWiredBeans().length > 0) {
			result.append("\n");
			result.append(Arrays.stream(moduleSingleSocketInfo.getWiredBeans()).map(beanQName -> pad + "  " + this.indent + "- " + beanQName.toString()).collect(Collectors.joining("\n")));
		}
		return result.toString();
	}

	@Override
	public String visit(MultiSocketBeanInfo moduleMultiSocketInfo, String pad) {
		StringBuilder result = new StringBuilder();
		
		result.append(this.visit((MultiSocketInfo)moduleMultiSocketInfo, pad)).append("\n");
		result.append(pad).append("  ").append("wiredTo:");
		if(moduleMultiSocketInfo.getWiredBeans().length > 0) {
			result.append("\n");
			result.append(Arrays.stream(moduleMultiSocketInfo.getWiredBeans()).map(beanQName -> pad + "  " + this.indent + "- " + beanQName.toString()).collect(Collectors.joining("\n")));
		}
		return result.toString();
	}
	
	@Override
	public String visit(SocketInfo socketInfo, String pad) {
		if(ModuleBeanSocketInfo.class.isAssignableFrom(socketInfo.getClass())) {
			return this.visit((ModuleBeanSocketInfo)socketInfo, pad);
		}
		else if(SocketBeanInfo.class.isAssignableFrom(socketInfo.getClass())) {
			return this.visit((SocketBeanInfo)socketInfo, pad);
		}
		else if(SingleSocketInfo.class.isAssignableFrom(socketInfo.getClass())) {
			return this.visit((SingleSocketInfo)socketInfo, pad);
		}
		else if(MultiSocketInfo.class.isAssignableFrom(socketInfo.getClass())) {
			return this.visit((MultiSocketInfo)socketInfo, pad);
		}
		return "";
	}

	@Override
	public String visit(SingleSocketInfo singleSocketInfo, String pad) {
		StringBuilder result = new StringBuilder();
		
		result.append(pad).append("- ").append("name: ").append(singleSocketInfo.getQualifiedName().getSimpleValue()).append("\n");
		result.append(pad).append("  ").append("type: ").append(singleSocketInfo.getType().toString()).append("\n");
		result.append(pad).append("  ").append("optional: ").append(singleSocketInfo.isOptional()).append("\n");
		result.append(pad).append("  ").append("socket: ");
		if(singleSocketInfo.getSocketElement().isPresent()) {
			result.append(singleSocketInfo.getSocketElement().get().toString());
		}
		result.append("\n").append(pad).append("  ").append("bean: ");
		if(singleSocketInfo.isResolved()) {
			result.append(singleSocketInfo.getBean().getQualifiedName().toString());
		}
		result.append("\n");
		result.append(pad).append("  ").append("selectors:");
		if(singleSocketInfo.getSelectors().length > 0) {
			result.append("\n");
			result.append(Arrays.stream(singleSocketInfo.getSelectors()).map(selector -> pad + "  " + this.indent + "- " + selector.toString()).collect(Collectors.joining("\n")));
		}
		
		return result.toString();
	}

	@Override
	public String visit(MultiSocketInfo multiSocketInfo, String pad) {
		StringBuilder result = new StringBuilder();
		
		result.append(pad).append("- ").append("name: ").append(multiSocketInfo.getQualifiedName().getSimpleValue()).append("\n");
		
		String type = null;
		if(multiSocketInfo.getMultiType().equals(MultiSocketType.ARRAY)) {
			type = multiSocketInfo.getType().toString()+"[]";
		}
		else if(multiSocketInfo.getMultiType().equals(MultiSocketType.COLLECTION)) {
			type = "java.util.Collection<" + multiSocketInfo.getType().toString() + ">";
		}
		else if(multiSocketInfo.getMultiType().equals(MultiSocketType.LIST)) {
			type = "java.util.List<" + multiSocketInfo.getType().toString() + ">";
		}
		else if(multiSocketInfo.getMultiType().equals(MultiSocketType.SET)) {
			type = "java.util.Set<" + multiSocketInfo.getType().toString() + ">";
		}
		result.append(pad).append("  ").append("type: ").append(type).append("\n");
		result.append(pad).append("  ").append("optional: ").append(multiSocketInfo.isOptional()).append("\n");
		result.append(pad).append("  ").append("socket: ");
		if(multiSocketInfo.getSocketElement().isPresent()) {
			result.append(multiSocketInfo.getSocketElement().get().toString());
		}
		result.append("\n").append(pad).append("  ").append("beans: ");
		if(multiSocketInfo.isResolved()) {
			result.append("\n");
			result.append(Arrays.stream(multiSocketInfo.getBeans()).map(bean -> pad + "  " + this.indent + "- " + bean.getQualifiedName().toString()).collect(Collectors.joining("\n")));
		}
		result.append("\n");
		result.append(pad).append("  ").append("selectors:");
		if(multiSocketInfo.getSelectors().length > 0) {
			result.append("\n");
			result.append(Arrays.stream(multiSocketInfo.getSelectors()).map(selector -> pad + "  " + this.indent + "- " + selector.toString()).collect(Collectors.joining("\n")));
		}
		return result.toString();
	}
}
