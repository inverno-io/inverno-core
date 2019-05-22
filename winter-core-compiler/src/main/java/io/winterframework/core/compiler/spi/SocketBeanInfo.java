/**
 * 
 */
package io.winterframework.core.compiler.spi;

import javax.lang.model.type.TypeMirror;

/**
 * @author jkuhn
 *
 */
public interface SocketBeanInfo extends BeanInfo, SocketInfo  {

	TypeMirror getSocketType();
	
	BeanQualifiedName[] getWiredBeans();
}
