/**
 * 
 */
package io.winterframework.core.compiler.spi;

/**
 * @author jkuhn
 *
 */
public interface ModuleInfoBuilder {

	ModuleQualifiedName getQualifiedName();
	
	ModuleInfoBuilder beans(ModuleBeanInfo[] beans);
	
	ModuleInfoBuilder sockets(SocketBeanInfo[] sockets);
	
	ModuleInfoBuilder modules(ModuleInfo[] modules);
	
	ModuleInfo build();
}
