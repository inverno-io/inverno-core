package io.winterframework.test.selector.moduleA;

import io.winterframework.core.annotation.AnnotationSelector;
import io.winterframework.core.annotation.Bean;

@Bean
public class BeanA {

	public Runnable runnable;
	
	public BeanA(@AnnotationSelector(Deprecated.class) Runnable runnable) {
		this.runnable = runnable;
	}
}
