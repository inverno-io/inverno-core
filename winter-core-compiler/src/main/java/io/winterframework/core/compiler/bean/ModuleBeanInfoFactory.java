/**
 * 
 */
package io.winterframework.core.compiler.bean;

import java.util.List;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.ModuleElement;

import io.winterframework.core.compiler.TypeErrorException;
import io.winterframework.core.compiler.common.AbstractInfoFactory;
import io.winterframework.core.compiler.spi.ModuleBeanInfo;
import io.winterframework.core.compiler.spi.SocketBeanInfo;

/**
 * @author jkuhn
 *
 */
public abstract class ModuleBeanInfoFactory extends AbstractInfoFactory {

	/**
	 * 
	 */
	protected ModuleBeanInfoFactory(ProcessingEnvironment processingEnvironment, ModuleElement moduleElement) {
		super(processingEnvironment, moduleElement);
	}

	public static ModuleBeanInfoFactory create(ProcessingEnvironment processingEnvironment, ModuleElement moduleElement) {
		return new CompiledModuleBeanInfoFactory(processingEnvironment, moduleElement);
	}
	
	public static ModuleBeanInfoFactory create(ProcessingEnvironment processingEnvironment, ModuleElement moduleElement, ModuleElement importedModuleElement, List<? extends SocketBeanInfo> moduleSocketInfos) {
		if(moduleElement.getDirectives().stream().noneMatch(directive -> directive.getKind().equals(ModuleElement.DirectiveKind.REQUIRES) && ((ModuleElement.RequiresDirective)directive).getDependency().equals(importedModuleElement))) {
			throw new IllegalArgumentException("The specified element is not imported in module " + moduleElement.getQualifiedName().toString());
		}
		return new ImportedModuleBeanInfoFactory(processingEnvironment, importedModuleElement, moduleElement, moduleSocketInfos);
	}
	
	public abstract ModuleBeanInfo createBean(Element element) throws BeanCompilationException, TypeErrorException;
}
