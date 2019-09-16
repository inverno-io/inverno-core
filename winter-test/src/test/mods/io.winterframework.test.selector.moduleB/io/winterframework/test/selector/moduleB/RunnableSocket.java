package io.winterframework.test.selector.moduleB;

import io.winterframework.core.annotation.AnnotationSelector;
import io.winterframework.core.annotation.Bean;

import java.util.List;
import java.util.function.Supplier;

@Bean
@AnnotationSelector(Deprecated.class) 
public interface RunnableSocket extends Supplier<Runnable> {

}
