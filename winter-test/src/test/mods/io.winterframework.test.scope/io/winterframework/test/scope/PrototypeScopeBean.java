package io.winterframework.test.scope;

import io.winterframework.core.annotation.Scope;
import io.winterframework.core.annotation.Scope.Type;
import io.winterframework.core.annotation.Bean;

@Bean
@Scope(Type.PROTOTYPE)
public class PrototypeScopeBean {

}
