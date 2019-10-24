package io.winterframework.test.lifecycle.moduleB;

import io.winterframework.core.annotation.Bean;
import io.winterframework.core.annotation.Destroy;
import io.winterframework.core.annotation.Init;
import io.winterframework.core.annotation.Scope;
import io.winterframework.core.annotation.Scope.Type;

@Bean
public class BeanD {

	public BeanC beanC;
	public BeanA beanA;
	
	public boolean destroyed;
	
	public boolean destroyFailed;
	
	public BeanD(BeanC beanC, BeanA beanA) {
		this.beanC = beanC;
		this.beanA = beanA;
	}
	
	@Destroy
	public void destroy() {
		this.destroyFailed = this.beanC.destroyed || this.beanA.destroyed;
		this.destroyed = true;
	}
}
