/**
 * 
 */
package io.winterframework.core.compiler.wire;

import java.util.Arrays;
import java.util.Map.Entry;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.type.TypeMirror;

import io.winterframework.core.annotation.AnnotationSelector;
import io.winterframework.core.compiler.spi.BeanInfo;
import io.winterframework.core.compiler.spi.SocketInfo;
import io.winterframework.core.compiler.spi.WiringStrategy;

/**
 * @author jkuhn
 *
 */
public class AnnotationSelectorWiringStrategy implements WiringStrategy {

	private ProcessingEnvironment processingEnvironment;
	
	private TypeMirror annotationSelectorAnnotationType;
	
	public AnnotationSelectorWiringStrategy(ProcessingEnvironment processingEnvironment) {
		this.processingEnvironment = processingEnvironment;
		
		this.annotationSelectorAnnotationType = this.processingEnvironment.getElementUtils().getTypeElement(AnnotationSelector.class.getCanonicalName()).asType();
	}
	
	@Override
	public boolean isWirable(BeanInfo bean, SocketInfo socket) {
		return Arrays.stream(socket.getSelectors())
			.filter(selector -> this.processingEnvironment.getTypeUtils().isSameType(selector.getAnnotationType(), this.annotationSelectorAnnotationType))
			.allMatch(annotationSelector -> {
				final TypeMirror annotionTypeToSelect = this.getAnnotationTypeToSelect(annotationSelector);
				return this.processingEnvironment.getTypeUtils().asElement(bean.getType()).getAnnotationMirrors().stream()
					.anyMatch(a -> this.processingEnvironment.getTypeUtils().isSameType(a.getAnnotationType(), annotionTypeToSelect));
			});
	}

	
	private TypeMirror getAnnotationTypeToSelect(AnnotationMirror annotationSelector) {
		for(Entry<? extends ExecutableElement, ? extends AnnotationValue> value : this.processingEnvironment.getElementUtils().getElementValuesWithDefaults(annotationSelector).entrySet()) {
			switch(value.getKey().getSimpleName().toString()) {
				case "value" : return (TypeMirror)value.getValue().getValue();
			}
		}
		// This should never happen
		throw new RuntimeException("Fatal error extracting annotation type to select");
	}
}
