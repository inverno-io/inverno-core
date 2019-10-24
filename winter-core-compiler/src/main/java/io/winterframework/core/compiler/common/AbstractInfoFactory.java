/**
 * 
 */
package io.winterframework.core.compiler.common;

import java.util.Map.Entry;
import java.util.Optional;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.ModuleElement;
import javax.lang.model.type.TypeMirror;

import io.winterframework.core.annotation.Module;
import io.winterframework.core.compiler.spi.ModuleQualifiedName;

/**
 * @author jkuhn
 *
 */
public abstract class AbstractInfoFactory {

	protected ProcessingEnvironment processingEnvironment;

	protected ModuleElement moduleElement;

	protected AnnotationMirror moduleAnnotation;
	
	protected ModuleQualifiedName moduleQName;
	
	/**
	 * 
	 */
	protected AbstractInfoFactory(ProcessingEnvironment processingEnvironment, ModuleElement moduleElement) {
		this.processingEnvironment = processingEnvironment;
		this.moduleElement = moduleElement;
		
		TypeMirror moduleAnnotationType = this.processingEnvironment.getElementUtils().getTypeElement(Module.class.getCanonicalName()).asType();
		Optional<? extends AnnotationMirror> moduleAnnotation = this.processingEnvironment.getElementUtils().getAllAnnotationMirrors(moduleElement).stream().filter(a -> this.processingEnvironment.getTypeUtils().isSameType(a.getAnnotationType(), moduleAnnotationType)).findFirst();
		if(!moduleAnnotation.isPresent()) {
			throw new IllegalArgumentException("The specified element is not annotated with " + Module.class.getSimpleName());
		}
		
		this.moduleAnnotation = moduleAnnotation.get();
		
		String packageName = "";
		String moduleName = "";
		String moduleClassName = null;
		
		for(Entry<? extends ExecutableElement, ? extends AnnotationValue> value : this.processingEnvironment.getElementUtils().getElementValuesWithDefaults(this.moduleAnnotation).entrySet()) {
			switch(value.getKey().getSimpleName().toString()) {
				case "className" : moduleClassName = (String)value.getValue().getValue();
					break;
			}
		}
		
		if(moduleElement.isUnnamed()) {
			moduleName = "DefaultModule";
		}
		else {
			packageName = this.moduleElement.getQualifiedName().toString();
			packageName = packageName.substring(0, packageName.lastIndexOf("."));
			
			moduleName = this.moduleElement.getQualifiedName().toString().substring(packageName.length() + 1);
		}
		
		this.moduleQName = new ModuleQualifiedName(packageName, moduleName, moduleClassName);
	}
	
	protected ReporterInfo getReporter(Element element) {
		return new ReporterInfo(this.processingEnvironment, element);
	}
	
	protected ReporterInfo getReporter(Element element, AnnotationMirror annotation) {
		return new ReporterInfo(this.processingEnvironment, element, annotation);
	}

}
