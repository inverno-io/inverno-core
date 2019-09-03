/**
 * 
 */
package io.winterframework.core.compiler.bean;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.ModuleElement;
import javax.lang.model.element.TypeElement;

import io.winterframework.core.compiler.spi.BeanQualifiedName;
import io.winterframework.core.compiler.spi.ModuleBeanInfo;
import io.winterframework.core.compiler.spi.ModuleBeanSocketInfo;
import io.winterframework.core.compiler.spi.SocketBeanInfo;

/**
 * @author jkuhn
 *
 */
class ImportedModuleBeanInfoFactory extends ModuleBeanInfoFactory {

	private ModuleElement compiledModuleElement;
	
	private Map<BeanQualifiedName, List<SocketBeanInfo>> moduleSocketInfosByWiredBeanQName;
	
	/**
	 * @param processingEnvironment
	 * @param moduleElement
	 */
	public ImportedModuleBeanInfoFactory(ProcessingEnvironment processingEnvironment, ModuleElement moduleElement, ModuleElement compiledModuleElement, List<? extends SocketBeanInfo> moduleSocketInfos) {
		super(processingEnvironment, moduleElement);
		
		this.compiledModuleElement = compiledModuleElement;
		
		this.moduleSocketInfosByWiredBeanQName = new HashMap<>();
		for(SocketBeanInfo moduleSocketInfo : moduleSocketInfos) {
			for(BeanQualifiedName wiredBeanQName : moduleSocketInfo.getWiredBeans()) {
				if(this.moduleSocketInfosByWiredBeanQName.get(wiredBeanQName) == null) {
					this.moduleSocketInfosByWiredBeanQName.put(wiredBeanQName, new ArrayList<>());
				}
				this.moduleSocketInfosByWiredBeanQName.get(wiredBeanQName).add(moduleSocketInfo);
			}
		}
	}

	/* (non-Javadoc)
	 * @see io.winterframework.core.compiler.model.ModuleBeanInfoFactory#createBean(javax.lang.model.element.Element)
	 */
	@Override
	public ModuleBeanInfo createBean(Element element) {
		if(!element.getKind().equals(ElementKind.METHOD)) {
			throw new IllegalArgumentException("Element must be a Method");
		}
		
		ExecutableElement executableElement = (ExecutableElement)element;
		if(!((TypeElement)executableElement.getEnclosingElement()).getQualifiedName().toString().equals(this.moduleQName.getClassName())) {
			throw new IllegalArgumentException("The specified element doesn't belong to module " + this.moduleQName);
		}
		if(!executableElement.getModifiers().contains(Modifier.PUBLIC) || executableElement.getParameters().size() != 0) {
			throw new IllegalArgumentException("Module bean " + new BeanQualifiedName(this.moduleQName, executableElement.getSimpleName().toString()) + " must be referenced as a public no-argument method");
		}
		
		BeanQualifiedName beanQName = new BeanQualifiedName(this.moduleQName, executableElement.getSimpleName().toString());
		ModuleBeanSocketInfoFactory beanSocketInfoFactory = ModuleBeanSocketInfoFactory.create(this.processingEnvironment, this.moduleElement, beanQName);
		
		List<? extends ModuleBeanSocketInfo> beanSocketInfos = null;
		if(this.moduleSocketInfosByWiredBeanQName.containsKey(beanQName)) {
			beanSocketInfos = this.moduleSocketInfosByWiredBeanQName.get(beanQName).stream()
				.map(moduleSocketInfo -> {
					CommonModuleBeanSingleSocketInfo beanSingleSocketInfo = (CommonModuleBeanSingleSocketInfo)beanSocketInfoFactory.createBeanSocket(moduleSocketInfo);
					beanSingleSocketInfo.setBean(moduleSocketInfo);
					return beanSingleSocketInfo;
				})
				.collect(Collectors.toList());
		}
		// Use compiledModuleElement instead of moduleElement to report compilation errors on the compiled module
		return new CommonModuleBeanInfo(this.processingEnvironment, this.compiledModuleElement, null, beanQName, executableElement.getReturnType(), null, beanSocketInfos);
	}
}
