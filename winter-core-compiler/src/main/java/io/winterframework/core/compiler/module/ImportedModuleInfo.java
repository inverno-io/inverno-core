/**
 * 
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
 * @author jkuhn
 *
 */
class ImportedModuleInfo extends AbstractInfo<ModuleQualifiedName> implements ModuleInfo {

	private boolean faulty;
	
	private List<SocketBeanInfo> socketInfos;
	
	private List<ModuleBeanInfo> beanInfos;
	
	public ImportedModuleInfo(ProcessingEnvironment processingEnvironment, Element element, ModuleQualifiedName qname, List<ModuleBeanInfo> beanInfos, List<SocketBeanInfo> socketInfos) {
		super(processingEnvironment, element, qname);
		
		if(beanInfos.stream().anyMatch(beanInfo -> !beanInfo.getVisibility().equals(Bean.Visibility.PUBLIC))) {
			throw new IllegalArgumentException("Only public beans can be injected to an imported module");
		}
		this.beanInfos = beanInfos != null ? Collections.unmodifiableList(beanInfos) : Collections.emptyList();
		this.socketInfos = socketInfos != null ? Collections.unmodifiableList(socketInfos) : Collections.emptyList();
	}

	void setFaulty(boolean faulty) {
		this.faulty = faulty;
	}
	
	/*
	 * (non-Javadoc)
	 * @see io.winterframework.core.compiler.spi.ModuleInfo#isFaulty()
	 */
	@Override
	public boolean isFaulty() {
		return this.faulty;
	}
	
	/* (non-Javadoc)
	 * @see io.winterframework.core.compiler.spi.Info#getQualifiedName()
	 */
	@Override
	public ModuleQualifiedName getQualifiedName() {
		return this.qname;
	}
	
	/* (non-Javadoc)
	 * @see io.winterframework.core.compiler.spi.ModuleInfo#getModules()
	 */
	@Override
	public ModuleInfo[] getModules() {
		return new ModuleInfo[0];
	}

	/* (non-Javadoc)
	 * @see io.winterframework.core.compiler.spi.ModuleInfo#getSockets()
	 */
	@Override
	public SocketBeanInfo[] getSockets() {
		return this.socketInfos.stream().toArray(SocketBeanInfo[]::new);
	}

	/* (non-Javadoc)
	 * @see io.winterframework.core.compiler.spi.ModuleInfo#getBean(java.lang.String)
	 */
	@Override
	public ModuleBeanInfo getBean(String name) {
		Optional<ModuleBeanInfo> moduleBean = this.beanInfos.stream().filter(bean -> bean.getQualifiedName().getSimpleValue().equals(name)).findFirst();
		return moduleBean.isPresent() ? moduleBean.get() : null;
	}

	/* (non-Javadoc)
	 * @see io.winterframework.core.compiler.spi.ModuleInfo#getBeans()
	 */
	@Override
	public ModuleBeanInfo[] getBeans() {
		return this.beanInfos.stream().toArray(ModuleBeanInfo[]::new);
	}

	/* (non-Javadoc)
	 * @see io.winterframework.core.compiler.spi.ModuleInfo#getPrivateBeans()
	 */
	@Override
	public ModuleBeanInfo[] getPrivateBeans() {
		return new ModuleBeanInfo[0];
	}

	/* (non-Javadoc)
	 * @see io.winterframework.core.compiler.spi.ModuleInfo#getPublicBeans()
	 */
	@Override
	public ModuleBeanInfo[] getPublicBeans() {
		return this.getBeans();
	}

	@Override
	public <R, P> R accept(ModuleInfoVisitor<R, P> visitor, P p) {
		return visitor.visit(this,  p);
	}
}
