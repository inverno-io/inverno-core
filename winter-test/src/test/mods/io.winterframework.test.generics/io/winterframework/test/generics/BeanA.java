package io.winterframework.test.generics;

import java.util.List;
import io.winterframework.core.annotation.Bean;

@Bean
public class BeanA {
	
	// ABE
	public List<Service<Action>> servicesAction;
	// ABCDEF
	public List<Service<? extends Action>> servicesExtendAction;
	// CDF
	public List<Service<CustomAction>> servicesCustomAction;
	// CDF
	public List<Service<? extends CustomAction>> servicesExtendCustomAction;
	// E
	public List<CustomService<Action>> customServicesAction;
	// EF
	public List<CustomService<? extends Action>> customServicesExtendsAction;
	// F
	public List<CustomService<CustomAction>> customServicesCustomAction;
	// F
	public List<CustomService<? extends CustomAction>> customServicesExtendsCustomAction;
	
	public BeanA(List<Service<Action>> servicesAction, 
		List<Service<? extends Action>> servicesExtendAction,
		List<Service<CustomAction>> servicesCustomAction, 
		List<Service<? extends CustomAction>> servicesExtendCustomAction,
		List<CustomService<Action>> customServicesAction,
		List<CustomService<? extends Action>> customServicesExtendsAction,
		List<CustomService<CustomAction>> customServicesCustomAction,
		List<CustomService<? extends CustomAction>> customServicesExtendsCustomAction) {
		
		this.servicesAction = servicesAction;
		this.servicesExtendAction = servicesExtendAction;
		this.servicesCustomAction = servicesCustomAction;
		this.servicesExtendCustomAction = servicesExtendCustomAction;
		this.customServicesAction = customServicesAction;
		this.customServicesExtendsAction = customServicesExtendsAction;
		this.customServicesCustomAction = customServicesCustomAction;
		this.customServicesExtendsCustomAction = customServicesExtendsCustomAction;
	}
}
