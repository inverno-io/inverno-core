package io.winterframework.test.lifecycle;

import io.winterframework.core.annotation.Bean;
import io.winterframework.core.annotation.Destroy;
import io.winterframework.core.annotation.Init;
import io.winterframework.core.annotation.Scope;
import io.winterframework.core.annotation.Scope.Type;

@Bean
@Scope(Type.SINGLETON)
public class SingletonScopeBean {

	public int initCount;
	
	public int destroyCount;
	
	public boolean beanInjected;
	
	public InjectedBean bean;
	
	public SingletonScopeBean(InjectedBean bean) {
		this.bean = bean;
	}
	
	@Init
	public void init() {
		this.initCount++;
		this.beanInjected = this.bean != null;
	}
	
	@Destroy
	public void destroy() {
		this.destroyCount++;
	}
}
