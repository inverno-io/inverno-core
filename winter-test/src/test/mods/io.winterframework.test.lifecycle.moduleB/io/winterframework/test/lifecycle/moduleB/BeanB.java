package io.winterframework.test.lifecycle.moduleB;

import io.winterframework.core.annotation.Bean;
import io.winterframework.core.annotation.Destroy;
import io.winterframework.core.annotation.Init;
import io.winterframework.core.annotation.Scope;
import io.winterframework.core.annotation.Scope.Type;

@Bean
public class BeanB {

	public BeanC beanC;
	
	public boolean destroyed;
	
	public boolean destroyFailed;
	
	public BeanB(BeanC beanC) {
		this.beanC = beanC; 
	}
	
	@Destroy
	public void destroy() {
		this.destroyFailed = this.beanC.destroyed;
		this.destroyed = true;
	}
}
