/**
 * 
 */
package io.winterframework.core.compiler.spi;

/**
 * @author jkuhn
 *
 */
public interface WiringStrategy {

	boolean isWirable(BeanInfo bean, SocketInfo socket);
}
