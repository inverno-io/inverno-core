package io.winterframework.test.lifecycle.moduleA;

import io.winterframework.core.annotation.Bean;
import io.winterframework.core.annotation.Destroy;
import io.winterframework.core.annotation.Init;
import io.winterframework.core.annotation.Scope;
import io.winterframework.core.annotation.Scope.Type;

@Bean
@Scope(Type.PROTOTYPE)
public class PrototypeScopeBean {

	public int initCount;
	
	public int destroyCount;
	
	public boolean beanInjected;
	
	public InjectedBean bean;
	
	public PrototypeScopeBean(InjectedBean bean) {
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
