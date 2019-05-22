package io.winterframework.core.compiler.spi;

public interface ModuleInfo extends Info {

	ModuleQualifiedName getQualifiedName();
	
	ModuleInfo[] getModules();
	
	SocketBeanInfo[] getSockets();
	
	ModuleBeanInfo getBean(String name);
	
	ModuleBeanInfo[] getBeans();
	
	ModuleBeanInfo[] getPrivateBeans();
	
	ModuleBeanInfo[] getPublicBeans();
	
	<R,P> R accept(ModuleInfoVisitor<R, P> visitor, P p);
	
	boolean isFaulty();
}
