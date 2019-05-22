/**
 * 
 */
package io.winterframework.core.compiler.spi;

import javax.lang.model.type.TypeMirror;

/**
 * @author jkuhn
 *
 */
public interface WrapperBeanInfo extends ModuleBeanInfo {

	TypeMirror getWrapperType();
}
