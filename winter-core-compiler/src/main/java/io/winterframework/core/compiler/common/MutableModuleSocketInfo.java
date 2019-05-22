/**
 * 
 */
package io.winterframework.core.compiler.common;

import io.winterframework.core.compiler.spi.SocketBeanInfo;

/**
 * @author jkuhn
 *
 */
public interface MutableModuleSocketInfo extends SocketBeanInfo {

	void setOptional(boolean optional);
}
