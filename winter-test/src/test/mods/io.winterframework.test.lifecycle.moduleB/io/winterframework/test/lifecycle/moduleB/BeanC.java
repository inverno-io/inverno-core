package io.winterframework.test.lifecycle.moduleB;

import io.winterframework.core.annotation.Bean;
import io.winterframework.core.annotation.Destroy;
import io.winterframework.core.annotation.Init;
import io.winterframework.core.annotation.Scope;
import io.winterframework.core.annotation.Scope.Type;

@Bean
public class BeanC {

	public BeanA beanA;
	
	public boolean destroyed;
	
	public boolean destroyFailed;
	
	public BeanC(BeanA beanA) {
		this.beanA = beanA;
	}
	
	@Destroy
	public void destroy() {
		this.destroyFailed = this.beanA.destroyed;
		this.destroyed = true;
	}
}
