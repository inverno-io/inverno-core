/**
 * 
 */
package io.winterframework.core.compiler.socket;

import java.util.Set;

import io.winterframework.core.compiler.spi.BeanQualifiedName;
import io.winterframework.core.compiler.spi.SocketBeanInfo;

/**
 * @author jkuhn
 *
 */
public interface WirableSocketBeanInfo extends SocketBeanInfo {

	void setWiredBeans(Set<BeanQualifiedName> wiredBeans);
}
