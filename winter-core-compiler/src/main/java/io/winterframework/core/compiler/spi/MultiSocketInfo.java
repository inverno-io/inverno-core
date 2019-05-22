/**
 * 
 */
package io.winterframework.core.compiler.spi;

/**
 * @author jkuhn
 *
 */
public interface MultiSocketInfo extends SocketInfo {

	BeanInfo[] getBeans();
	
	MultiSocketType getMultiType();
}
