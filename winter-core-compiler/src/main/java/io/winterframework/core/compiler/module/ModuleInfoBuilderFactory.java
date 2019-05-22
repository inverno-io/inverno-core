/**
 * 
 */
package io.winterframework.core.compiler.module;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.ModuleElement;

import io.winterframework.core.compiler.spi.ModuleInfoBuilder;

/**
 * @author jkuhn
 *
 */
public abstract class ModuleInfoBuilderFactory {

	public static ModuleInfoBuilder createModuleBuilder(ProcessingEnvironment processingEnvironment, ModuleElement moduleElement) {
		return new CompiledModuleInfoBuilder(processingEnvironment, moduleElement);
	}
	
	public static ModuleInfoBuilder createModuleBuilder(ProcessingEnvironment processingEnvironment, ModuleElement moduleElement, ModuleElement importedModuleElement) {
		if(moduleElement.getDirectives().stream().noneMatch(directive -> directive.getKind().equals(ModuleElement.DirectiveKind.REQUIRES) && ((ModuleElement.RequiresDirective)directive).getDependency().equals(importedModuleElement))) {
			throw new IllegalArgumentException("The specified element is not imported in module " + moduleElement.getQualifiedName().toString());
		}
		return new ImportedModuleInfoBuilder(processingEnvironment, importedModuleElement);
	}
}
