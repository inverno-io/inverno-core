package io.winterframework.test.lifecycle.moduleB;

import io.winterframework.core.annotation.Bean;
import io.winterframework.core.annotation.Destroy;
import io.winterframework.core.annotation.Init;
import io.winterframework.core.annotation.Scope;
import io.winterframework.core.annotation.Scope.Type;

@Bean
public class BeanA {

	public boolean destroyed;
	
	public boolean destroyFailed;
	
	public BeanA() {
	}
	
	@Destroy
	public void destroy() {
		this.destroyFailed = false;
		this.destroyed = true;
	}
}
