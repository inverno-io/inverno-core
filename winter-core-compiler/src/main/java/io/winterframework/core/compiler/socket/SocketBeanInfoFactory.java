/**
 * 
 */
package io.winterframework.core.compiler.socket;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.ModuleElement;

import io.winterframework.core.compiler.TypeErrorException;
import io.winterframework.core.compiler.common.AbstractSocketInfoFactory;

/**
 * @author jkuhn
 *
 */
public abstract class SocketBeanInfoFactory extends AbstractSocketInfoFactory {

	/**
	 * @param processingEnvironment
	 * @param moduleElement
	 */
	protected SocketBeanInfoFactory(ProcessingEnvironment processingEnvironment, ModuleElement moduleElement) {
		super(processingEnvironment, moduleElement);
	}

	public static SocketBeanInfoFactory create(ProcessingEnvironment processingEnvironment, ModuleElement moduleElement) {
		return new CompiledSocketBeanInfoFactory(processingEnvironment, moduleElement);
	}
	
	public static SocketBeanInfoFactory create(ProcessingEnvironment processingEnvironment, ModuleElement moduleElement, ModuleElement importedModuleElement, Integer version) {
		if(moduleElement.getDirectives().stream().noneMatch(directive -> directive.getKind().equals(ModuleElement.DirectiveKind.REQUIRES) && ((ModuleElement.RequiresDirective)directive).getDependency().equals(importedModuleElement))) {
			throw new IllegalArgumentException("The specified element is not imported in module " + moduleElement.getQualifiedName().toString());
		}
		
		if(version == null) {
			throw new IllegalStateException("Version of imported module can't be null");			
		}
		switch(version) {
			case 1: return new ImportedSocketBeanInfoFactory(processingEnvironment, importedModuleElement, moduleElement);
			default: throw new IllegalStateException("Unsupported version: " + version);
		}
	}
	
	public abstract WirableSocketBeanInfo createModuleSocket(Element element) throws SocketCompilationException, TypeErrorException;
}
