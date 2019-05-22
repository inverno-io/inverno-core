/**
 * 
 */
package io.winterframework.core.compiler.spi;

import javax.lang.model.element.ExecutableElement;
import javax.lang.model.type.TypeMirror;

/**
 * @author jkuhn
 *
 */
public interface SocketInfo extends Info {

	TypeMirror getType();
	
	ExecutableElement getSocketElement();
	
	boolean isOptional();
	
	boolean isResolved();
}
