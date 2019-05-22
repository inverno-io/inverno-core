/**
 * 
 */
package io.winterframework.core.compiler.common;

import io.winterframework.core.compiler.spi.BeanInfo;
import io.winterframework.core.compiler.spi.MultiSocketInfo;

/**
 * @author jkuhn
 *
 */
public interface MutableMultiSocketInfo extends MultiSocketInfo {

	void setBeans(BeanInfo[] beanInfos);
}
