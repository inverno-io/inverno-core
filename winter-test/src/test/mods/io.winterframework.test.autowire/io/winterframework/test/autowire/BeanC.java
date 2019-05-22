package io.winterframework.test.autowire;

import io.winterframework.core.annotation.Bean;
import io.winterframework.core.annotation.Scope;
import io.winterframework.core.annotation.Scope.Type;

@Bean
@Scope(Type.PROTOTYPE)
public class BeanC {

}
