/**
 * 
 */
package io.winterframework.core.compiler.common;

import io.winterframework.core.compiler.spi.BeanInfo;
import io.winterframework.core.compiler.spi.SingleSocketInfo;

/**
 * @author jkuhn
 *
 */
public interface MutableSingleSocketInfo extends SingleSocketInfo {

	void setBean(BeanInfo bean);
}
