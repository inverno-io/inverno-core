/*
 * Copyright 2018 Jeremy KUHN
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.inverno.core.compiler.wire;

import java.util.Arrays;
import java.util.Map.Entry;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.type.TypeMirror;

import io.inverno.core.annotation.AnnotationSelector;
import io.inverno.core.compiler.spi.BeanInfo;
import io.inverno.core.compiler.spi.SocketInfo;
import io.inverno.core.compiler.spi.WiringStrategy;

/**
 * <p>
 * {@link WiringStrategy} implementation used to determine if a bean is wirable to a socket based on the value of {@link AnnotationSelector} annotation specified on the socket.
 * </p>
 *
 * @author <a href="mailto:jeremy.kuhn@inverno.io">Jeremy Kuhn</a>
 *
 */
public class AnnotationSelectorWiringStrategy implements WiringStrategy {

	private final ProcessingEnvironment processingEnvironment;
	
	private final TypeMirror annotationSelectorAnnotationType;
	
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
