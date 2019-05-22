package io.winterframework.test.scope;

import io.winterframework.core.annotation.Bean;
import io.winterframework.core.annotation.Scope;
import io.winterframework.core.annotation.Scope.Type;

@Bean
@Scope(Type.SINGLETON)
public class SingletonScopeBean {

}
