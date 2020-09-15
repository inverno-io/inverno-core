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
import io.winterframework.core.compiler.spi.ConfigurationInfo;
import io.winterframework.core.compiler.spi.ConfigurationPropertyInfo;
import io.winterframework.core.compiler.spi.ConfigurationSocketBeanInfo;
import io.winterframework.core.compiler.spi.ModuleBeanInfo;
import io.winterframework.core.compiler.spi.ModuleBeanMultiSocketInfo;
import io.winterframework.core.compiler.spi.ModuleBeanSingleSocketInfo;
import io.winterframework.core.compiler.spi.ModuleBeanSocketInfo;
import io.winterframework.core.compiler.spi.ModuleInfo;
import io.winterframework.core.compiler.spi.ModuleInfoVisitor;
import io.winterframework.core.compiler.spi.MultiSocketBeanInfo;
import io.winterframework.core.compiler.spi.MultiSocketInfo;
import io.winterframework.core.compiler.spi.MultiSocketType;
import io.winterframework.core.compiler.spi.NestedBeanInfo;
import io.winterframework.core.compiler.spi.NestedConfigurationPropertyInfo;
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
		String componentModules = Arrays.stream(moduleInfo.getModules()).map(module -> this.visit(module, pad + this.indent + this.indent)).collect(Collectors.joining("\n"));
		if(componentModules != null && !componentModules.equals("")) {
			result.append(componentModules).append("\n");
		}
		result.append(pad).append(this.indent).append("sockets:").append("\n");
		String sockets = Arrays.stream(moduleInfo.getSockets()).map(socket -> this.visit(socket, pad + this.indent + this.indent)).collect(Collectors.joining("\n"));
		if(sockets != null && !sockets.equals("")) {
			result.append(sockets).append("\n");
		}
		result.append(pad).append(this.indent).append("configurations:").append("\n");
		String configurations = Arrays.stream(moduleInfo.getConfigurations()).map(configuration -> this.visit(configuration, pad + this.indent + this.indent)).collect(Collectors.joining("\n"));
		if(configurations != null && !configurations.equals("")) {
			result.append(configurations).append("\n");
		}
		
		result.append(pad).append(this.indent).append("beans:").append("\n");

		result.append(pad).append(this.indent).append(this.indent).append("private:").append("\n");
		String privateBeans = Arrays.stream(moduleInfo.getPrivateBeans()).map(bean -> this.visit(bean, pad + this.indent + this.indent + this.indent)).collect(Collectors.joining("\n"));
		if(privateBeans != null && !privateBeans.equals("")) {
			result.append(privateBeans).append("\n");
		}
		
		result.append(pad).append(this.indent).append(this.indent).append("public:").append("\n");
		String publicBeans = Arrays.stream(moduleInfo.getPublicBeans()).map(bean -> this.visit(bean, pad + this.indent + this.indent + this.indent)).collect(Collectors.joining("\n"));
		if(publicBeans != null && !publicBeans.equals("")) {
			result.append(publicBeans);
		}
		return result.toString();
	}

	@Override
	public String visit(BeanInfo beanInfo, String pad) {
		if(beanInfo instanceof NestedBeanInfo) {
			return this.visit((NestedBeanInfo)beanInfo, pad);
		}
		else if(beanInfo instanceof ModuleBeanInfo) {
			return this.visit((ModuleBeanInfo)beanInfo, pad);
		}
		else if(beanInfo instanceof SocketBeanInfo) {
			return this.visit((SocketBeanInfo)beanInfo, pad);
		}
		return "";
	}

	@Override
	public String visit(NestedBeanInfo nestedBeanInfo, String pad) {
		StringBuilder result = new StringBuilder();
		result.append(pad).append("- ").append("name: ").append(nestedBeanInfo.getQualifiedName().getSimpleValue()).append("\n");
		result.append(pad).append("  ").append("type: ").append(nestedBeanInfo.getType().toString());
		return result.toString();
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
		if(moduleBeanInfo instanceof WrapperBeanInfo) {
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
		result.append(pad).append("  ").append("sockets:").append("\n");
		if(moduleBeanInfo.getSockets().length > 0) {
			result.append(Arrays.stream(moduleBeanInfo.getSockets()).map(socket -> this.visit(socket, pad + "  " + this.indent)).collect(Collectors.joining("\n"))).append("\n");
		}
		result.append(pad).append("  ").append("nestedBeans:");
		if(moduleBeanInfo.getNestedBeans().length > 0) {
			result.append("\n");
			result.append(Arrays.stream(moduleBeanInfo.getNestedBeans()).map(beanInfo -> this.visit(beanInfo, pad + "  " + this.indent)).collect(Collectors.joining("\n")));
		}
		return result.toString();
	}

	@Override
	public String visit(WrapperBeanInfo moduleWrapperBeanInfo, String pad) {
		return this.visit((ModuleBeanInfo)moduleWrapperBeanInfo, pad);
	}
	
	@Override
	public String visit(ModuleBeanSocketInfo beanSocketInfo, String pad) {
		StringBuilder result = new StringBuilder();
		if(beanSocketInfo instanceof ModuleBeanSingleSocketInfo) {
			result.append(this.visit((ModuleBeanSingleSocketInfo)beanSocketInfo, pad));
		}
		else if(beanSocketInfo instanceof ModuleBeanMultiSocketInfo) {
			result.append(this.visit((ModuleBeanMultiSocketInfo)beanSocketInfo, pad));
		}
		result.append("\n").append(pad).append("  ").append("lazy: ").append(beanSocketInfo.isLazy());
		return result.toString();
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
		StringBuilder result = new StringBuilder();
		
		if(moduleSocketInfo instanceof SingleSocketBeanInfo) {
			result.append(this.visit((SingleSocketInfo)moduleSocketInfo, pad));
		}
		else if(moduleSocketInfo instanceof MultiSocketBeanInfo) {
			result.append(this.visit((MultiSocketInfo)moduleSocketInfo, pad));
		}
		result.append("\n");
		result.append(pad).append("  ").append("socketType: ").append(moduleSocketInfo.getSocketType().toString()).append("\n");
		result.append(pad).append("  ").append("wired: ").append(moduleSocketInfo.isWired()).append("\n");
		result.append(pad).append("  ").append("wiredTo:").append("\n");
		if(moduleSocketInfo.getWiredBeans().length > 0) {
			result.append(Arrays.stream(moduleSocketInfo.getWiredBeans()).map(beanQName -> pad + "  " + this.indent + "- " + beanQName.toString()).collect(Collectors.joining("\n"))).append("\n");
		}
		result.append(pad).append("  ").append("nestedBeans:");
		if(moduleSocketInfo.getNestedBeans().length > 0) {
			result.append("\n");
			result.append(Arrays.stream(moduleSocketInfo.getNestedBeans()).map(beanInfo -> this.visit(beanInfo, pad + "  " + this.indent)).collect(Collectors.joining("\n")));
		}
		return result.toString();
	}

	@Override
	public String visit(SingleSocketBeanInfo singleSocketBeanInfo, String pad) {
		return this.visit((SocketBeanInfo)singleSocketBeanInfo, pad);
	}

	@Override
	public String visit(MultiSocketBeanInfo multiSocketBeanInfo, String pad) {
		return this.visit((SocketBeanInfo)multiSocketBeanInfo, pad);
	}
	
	@Override
	public String visit(SocketInfo socketInfo, String pad) {
		if(socketInfo instanceof ModuleBeanSocketInfo) {
			return this.visit((ModuleBeanSocketInfo)socketInfo, pad);
		}
		else if(socketInfo instanceof SocketBeanInfo) {
			return this.visit((SocketBeanInfo)socketInfo, pad);
		}
		else if(socketInfo instanceof SingleSocketInfo) {
			return this.visit((SingleSocketInfo)socketInfo, pad);
		}
		else if(socketInfo instanceof MultiSocketInfo) {
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
	
	@Override
	public String visit(ConfigurationInfo configurationInfo, String pad) {
		StringBuilder result = new StringBuilder();
		
		result.append(pad).append("- ").append("name: ").append(configurationInfo.getQualifiedName().getSimpleValue()).append("\n");
		result.append(pad).append("  ").append("type: ").append(configurationInfo.getType()).append("\n");
		result.append(pad).append("  ").append("properties: ").append("\n");
		result.append(Arrays.stream(configurationInfo.getProperties()).map(configurationPropertyInfo -> {
			if(configurationPropertyInfo instanceof NestedConfigurationPropertyInfo) {
				return this.visit((NestedConfigurationPropertyInfo)configurationPropertyInfo, pad + "  " + this.indent);
			}
			else {
				return this.visit(configurationPropertyInfo, pad + "  " + this.indent);
			}
		}).collect(Collectors.joining("\n")));
		if(configurationInfo.getSocket().isPresent()) {
			result.append("\n");
			result.append(pad).append("  ").append("socket: ").append("\n");
			result.append(this.visit(configurationInfo.getSocket().get(), pad + "  " + this.indent));
		}
		return result.toString();
	}
	
	@Override
	public String visit(ConfigurationPropertyInfo configurationPropertyInfo, String pad) {
		StringBuilder result = new StringBuilder();
		result.append(pad).append("- ").append("name: ").append(configurationPropertyInfo.getName()).append("\n");
		result.append(pad).append("  ").append("type: ").append(configurationPropertyInfo.getType().toString()).append("\n");
		result.append(pad).append("  ").append("hasDefault: ").append(configurationPropertyInfo.isDefault());
		return result.toString();
	}
	
	@Override
	public String visit(NestedConfigurationPropertyInfo nestedConfigurationPropertyInfo, String pad) {
		StringBuilder result = new StringBuilder();
		result.append(this.visit((ConfigurationPropertyInfo)nestedConfigurationPropertyInfo, pad)).append("\n");
		result.append(pad).append("  ").append("configuratorType: ").append(nestedConfigurationPropertyInfo.getConfiguratorClassName().toString());
		
		return result.toString(); 
	}
	
	@Override
	public String visit(ConfigurationSocketBeanInfo configurationSocketBeanInfo, String pad) {
		return this.visit((SingleSocketBeanInfo)configurationSocketBeanInfo, pad);
	}
}
