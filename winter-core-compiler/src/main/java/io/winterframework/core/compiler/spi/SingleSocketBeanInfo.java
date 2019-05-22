/**
 * 
 */
package io.winterframework.core.compiler.spi;

/**
 * @author jkuhn
 *
 */
public interface SingleSocketBeanInfo extends SocketBeanInfo, SingleSocketInfo {

	BeanInfo getBean();
}
