/**
 * 
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
 * @author jkuhn
 *
 */
class ModuleReporter implements ModuleInfoVisitor<String, String> {

	private String indent = DEFAULT_INDENT;
	
	private static final String DEFAULT_INDENT = "    ";
	
	public String getIndent() {
		return this.indent;
	}

	public void setIndent(String indent) {
		this.indent = indent;
	}

	/* (non-Javadoc)
	 * @see io.winterframework.core.compiler.spi.ModuleInfoVisitor#visit(io.winterframework.core.compiler.spi.ModuleInfo, java.lang.Object)
	 */
	@Override
	public String visit(ModuleInfo moduleInfo, String pad) {
		StringBuilder result = new StringBuilder();
		
		result.append(pad).append("module:").append("\n");
		result.append(pad).append(this.indent).append("name: ").append(moduleInfo.getQualifiedName().toString()).append("\n");
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

	/* (non-Javadoc)
	 * @see io.winterframework.core.compiler.spi.ModuleInfoVisitor#visit(io.winterframework.core.compiler.spi.BeanInfo, java.lang.Object)
	 */
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

	/* (non-Javadoc)
	 * @see io.winterframework.core.compiler.spi.ModuleInfoVisitor#visit(io.winterframework.core.compiler.spi.ModuleBeanInfo, java.lang.Object)
	 */
	@Override
	public String visit(ModuleBeanInfo moduleBeanInfo, String pad) {
		StringBuilder result = new StringBuilder();
		
		result.append(pad).append("- ").append("name: ").append(moduleBeanInfo.getQualifiedName().getSimpleValue()).append("\n");
		result.append(pad).append("  ").append("type: ").append(moduleBeanInfo.getType().toString()).append("\n");
		if(moduleBeanInfo.getProvidedType() != null) {
			result.append(pad).append("  ").append("providedType: ").append(moduleBeanInfo.getProvidedType().toString()).append("\n");
		}
		result.append(pad).append("  ").append("scope: ").append(moduleBeanInfo.getScope().toString()).append("\n");
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

	/* (non-Javadoc)
	 * @see io.winterframework.core.compiler.spi.ModuleInfoVisitor#visit(io.winterframework.core.compiler.spi.ModuleWrapperBeanInfo, java.lang.Object)
	 */
	@Override
	public String visit(WrapperBeanInfo moduleWrapperBeanInfo, String pad) {
		return this.visit((ModuleBeanInfo)moduleWrapperBeanInfo, pad);
	}
	
	/* (non-Javadoc)
	 * @see io.winterframework.core.compiler.spi.ModuleInfoVisitor#visit(io.winterframework.core.compiler.spi.ModuleBeanSocketInfo, java.lang.Object)
	 */
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

	/* (non-Javadoc)
	 * @see io.winterframework.core.compiler.spi.ModuleInfoVisitor#visit(io.winterframework.core.compiler.spi.ModuleBeanSingleSocketInfo, java.lang.Object)
	 */
	@Override
	public String visit(ModuleBeanSingleSocketInfo beanSingleSocketInfo, String pad) {
		return this.visit((SingleSocketInfo)beanSingleSocketInfo, pad);
	}

	/* (non-Javadoc)
	 * @see io.winterframework.core.compiler.spi.ModuleInfoVisitor#visit(io.winterframework.core.compiler.spi.ModuleBeanMultiSocketInfo, java.lang.Object)
	 */
	@Override
	public String visit(ModuleBeanMultiSocketInfo beanMultiSocketInfo, String pad) {
		return this.visit((MultiSocketInfo)beanMultiSocketInfo, pad);
	}

	/* (non-Javadoc)
	 * @see io.winterframework.core.compiler.spi.ModuleInfoVisitor#visit(io.winterframework.core.compiler.spi.ModuleSocketInfo, java.lang.Object)
	 */
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

	/* (non-Javadoc)
	 * @see io.winterframework.core.compiler.spi.ModuleInfoVisitor#visit(io.winterframework.core.compiler.spi.ModuleSingleSocketInfo, java.lang.Object)
	 */
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

	/* (non-Javadoc)
	 * @see io.winterframework.core.compiler.spi.ModuleInfoVisitor#visit(io.winterframework.core.compiler.spi.ModuleMultiSocketInfo, java.lang.Object)
	 */
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
	
	/* (non-Javadoc)
	 * @see io.winterframework.core.compiler.spi.ModuleInfoVisitor#visit(io.winterframework.core.compiler.spi.SocketInfo, java.lang.Object)
	 */
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

	/* (non-Javadoc)
	 * @see io.winterframework.core.compiler.spi.ModuleInfoVisitor#visit(io.winterframework.core.compiler.spi.SingleSocketInfo, java.lang.Object)
	 */
	@Override
	public String visit(SingleSocketInfo singleSocketInfo, String pad) {
		StringBuilder result = new StringBuilder();
		
		result.append(pad).append("- ").append("name: ").append(singleSocketInfo.getQualifiedName().getSimpleValue()).append("\n");
		result.append(pad).append("  ").append("type: ").append(singleSocketInfo.getType().toString()).append("\n");
		result.append(pad).append("  ").append("optional: ").append(singleSocketInfo.isOptional()).append("\n");
		result.append(pad).append("  ").append("socket: ");
		if(singleSocketInfo.getSocketElement() != null) {
			result.append(singleSocketInfo.getSocketElement().toString());
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

	/* (non-Javadoc)
	 * @see io.winterframework.core.compiler.spi.ModuleInfoVisitor#visit(io.winterframework.core.compiler.spi.MultiSocketInfo, java.lang.Object)
	 */
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
		if(multiSocketInfo.getSocketElement() != null) {
			result.append(multiSocketInfo.getSocketElement().toString());
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
