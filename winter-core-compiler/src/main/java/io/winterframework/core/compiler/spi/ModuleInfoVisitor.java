/**
 * 
 */
package io.winterframework.core.compiler.spi;

/**
 * @author jkuhn
 *
 */
public interface ModuleInfoVisitor<R, P> {

	R visit(ModuleInfo moduleInfo, P p);
	
	R visit(BeanInfo beanInfo, P p);
	
	R visit(ModuleBeanInfo moduleBeanInfo, P p);
	
	R visit(WrapperBeanInfo moduleWrapperBeanInfo, P p);
	
	R visit(SocketInfo socketInfo, P p);
	
	R visit(SingleSocketInfo singleSocketInfo, P p);
	
	R visit(MultiSocketInfo multiSocketInfo, P p);
	
	R visit(ModuleBeanSocketInfo beanSocketInfo, P p);
	
	R visit(ModuleBeanSingleSocketInfo beanSingleSocketInfo, P p);
	
	R visit(ModuleBeanMultiSocketInfo beanMultiSocketInfo, P p);
	
	R visit(SocketBeanInfo socketBeanInfo, P p);
	
	R visit(SingleSocketBeanInfo singleSocketBeanInfo, P p);
	
	R visit(MultiSocketBeanInfo multiSocketBeanInfo, P p);
}
