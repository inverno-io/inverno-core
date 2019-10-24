/**
 * 
 */
package io.winterframework.core.compiler.module;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.AnnotationMirror;
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
class CompiledModuleInfo extends AbstractInfo<ModuleQualifiedName> implements ModuleInfo {

	private int version;
	
	private boolean faulty;
	
	private List<ModuleBeanInfo> beanInfos;
	
	private List<SocketBeanInfo> socketInfos;
	
	private List<ModuleInfo> moduleInfos;
	
	public CompiledModuleInfo(ProcessingEnvironment processingEnvironment, Element element, AnnotationMirror annotation, ModuleQualifiedName qname, int version, List<ModuleBeanInfo> beanInfos, List<SocketBeanInfo> socketInfos, List<ModuleInfo> moduleInfos) {
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
		return this.socketInfos.stream().toArray(SocketBeanInfo[]::new);
	}

	@Override
	public ModuleBeanInfo getBean(String name) {
		Optional<ModuleBeanInfo> moduleBean = this.beanInfos.stream().filter(bean -> bean.getQualifiedName().getSimpleValue().equals(name)).findFirst();
		return moduleBean.isPresent() ? moduleBean.get() : null;
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
}
