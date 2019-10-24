package io.winterframework.test.lifecycle.moduleB;

import io.winterframework.core.annotation.Bean;
import io.winterframework.core.annotation.Destroy;
import io.winterframework.core.annotation.Init;
import io.winterframework.core.annotation.Scope;
import io.winterframework.core.annotation.Scope.Type;

@Bean
public class BeanE {

	public BeanB beanB;
	
	public boolean destroyed;
	
	public boolean destroyFailed;
	
	public BeanE(BeanB beanB) {
		this.beanB = beanB;
	}
	
	@Destroy
	public void destroy() {
		this.destroyFailed = this.beanB.destroyed;
		this.destroyed = true;
	}
}
