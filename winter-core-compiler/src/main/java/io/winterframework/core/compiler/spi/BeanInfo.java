/**
 * 
 */
package io.winterframework.core.compiler.spi;

import javax.lang.model.type.TypeMirror;

/**
 * @author jkuhn
 *
 */
public interface BeanInfo extends Info {
	
	BeanQualifiedName getQualifiedName();
	
	TypeMirror getType();
}
