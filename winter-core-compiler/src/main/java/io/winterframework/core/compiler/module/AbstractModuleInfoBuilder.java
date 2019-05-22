/**
 * 
 */
package io.winterframework.core.compiler.module;

import java.util.Map.Entry;
import java.util.Optional;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.ModuleElement;
import javax.lang.model.type.TypeMirror;

import io.winterframework.core.annotation.Module;
import io.winterframework.core.compiler.spi.ModuleInfoBuilder;
import io.winterframework.core.compiler.spi.ModuleQualifiedName;

/**
 * @author jkuhn
 *
 */
abstract class AbstractModuleInfoBuilder implements ModuleInfoBuilder {

	protected ProcessingEnvironment processingEnvironment;
	
	protected ModuleElement element;
	
	protected AnnotationMirror annotation;

	protected ModuleQualifiedName moduleQName;
	
	/**
	 * 
	 */
	public AbstractModuleInfoBuilder(ProcessingEnvironment processingEnvironment, ModuleElement element) {
		this.processingEnvironment = processingEnvironment;
		this.element = element;
		
		TypeMirror moduleAnnotationType = this.processingEnvironment.getElementUtils().getTypeElement(Module.class.getCanonicalName()).asType();
		Optional<? extends AnnotationMirror> moduleAnnotation = this.processingEnvironment.getElementUtils().getAllAnnotationMirrors(element).stream().filter(a -> this.processingEnvironment.getTypeUtils().isSameType(a.getAnnotationType(), moduleAnnotationType)).findFirst();
		if(!moduleAnnotation.isPresent()) {
			throw new IllegalArgumentException("The specified element is not annotated with " + Module.class.getSimpleName());
		}
		
		this.annotation = moduleAnnotation.get();
		
		String packageName = "";
		String moduleName = "";
		String moduleClassName = null;
		
		for(Entry<? extends ExecutableElement, ? extends AnnotationValue> value : this.processingEnvironment.getElementUtils().getElementValuesWithDefaults(this.annotation).entrySet()) {
			switch(value.getKey().getSimpleName().toString()) {
				case "className" : moduleClassName = (String)value.getValue().getValue();
					break;
			}
		}
		
		if(element.isUnnamed()) {
			moduleName = "DefaultModule";
		}
		else {
			packageName = this.element.getQualifiedName().toString();
			packageName = packageName.substring(0, packageName.lastIndexOf("."));
			
//			moduleName = this.element.getSimpleName().toString();
			moduleName = this.element.getQualifiedName().toString().substring(packageName.length() + 1);
		}
		
		this.moduleQName = new ModuleQualifiedName(packageName, moduleName, moduleClassName);
	}
}
