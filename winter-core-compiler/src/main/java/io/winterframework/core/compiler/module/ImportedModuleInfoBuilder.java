/**
 * 
 */
package io.winterframework.core.compiler.module;

import java.util.Arrays;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.ModuleElement;

import io.winterframework.core.annotation.Bean;
import io.winterframework.core.compiler.common.AbstractInfoFactory;
import io.winterframework.core.compiler.spi.ModuleBeanInfo;
import io.winterframework.core.compiler.spi.ModuleInfo;
import io.winterframework.core.compiler.spi.ModuleInfoBuilder;
import io.winterframework.core.compiler.spi.ModuleQualifiedName;
import io.winterframework.core.compiler.spi.SocketBeanInfo;

/**
 * @author jkuhn
 *
 */
class ImportedModuleInfoBuilder extends AbstractInfoFactory implements ModuleInfoBuilder {

	private ModuleBeanInfo[] beans;
	
	private SocketBeanInfo[] sockets;
	
	public ImportedModuleInfoBuilder(ProcessingEnvironment processingEnvironment, ModuleElement moduleElement) {
		super(processingEnvironment, moduleElement);
		
		this.beans = new ModuleBeanInfo[0];
		this.sockets = new SocketBeanInfo[0];
	}
	
	@Override
	public ModuleQualifiedName getQualifiedName() {
		return this.moduleQName;
	}
	
	/* (non-Javadoc)
	 * @see io.winterframework.core.compiler.spi.ModuleInfoBuilder#beans(io.winterframework.core.compiler.spi.ModuleBeanInfo[])
	 */
	@Override
	public ModuleInfoBuilder beans(ModuleBeanInfo[] beans) {
		if(beans == null) {
			this.beans = new ModuleBeanInfo[0];
		}
		else {
			if(Arrays.stream(beans).anyMatch(beanInfo -> !beanInfo.getVisibility().equals(Bean.Visibility.PUBLIC))) {
				throw new IllegalArgumentException("Only public beans can be injected to an imported module");
			}
			this.beans = beans;
		}
		return this;
	}

	/* (non-Javadoc)
	 * @see io.winterframework.core.compiler.spi.ModuleInfoBuilder#sockets(io.winterframework.core.compiler.spi.ModuleSocketInfo[])
	 */
	@Override
	public ModuleInfoBuilder sockets(SocketBeanInfo[] sockets) {
		this.sockets = sockets != null ? sockets : new SocketBeanInfo[0];
		return this;
	}

	/* (non-Javadoc)
	 * @see io.winterframework.core.compiler.spi.ModuleInfoBuilder#modules(io.winterframework.core.compiler.spi.ModuleInfo[])
	 */
	@Override
	public ModuleInfoBuilder modules(ModuleInfo[] modules) {
		throw new UnsupportedOperationException("You can't inject modules to an imported module");
	}

	/* (non-Javadoc)
	 * @see io.winterframework.core.compiler.spi.ModuleInfoBuilder#build()
	 */
	@Override
	public ModuleInfo build() {
		return new ImportedModuleInfo(this.processingEnvironment, this.moduleElement, this.moduleQName, Arrays.asList(this.beans), Arrays.asList(this.sockets));
	}

}
