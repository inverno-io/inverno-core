/**
 * 
 */
package io.winterframework.core.compiler.spi;

import javax.lang.model.element.ExecutableElement;
import javax.lang.model.type.TypeMirror;

import io.winterframework.core.annotation.Bean;
import io.winterframework.core.annotation.Scope;

/**
 * @author jkuhn
 *
 */
public interface ModuleBeanInfo extends BeanInfo {

	TypeMirror getProvidedType();
	
	Scope.Type getScope();
	
	Bean.Visibility getVisibility();
	
	ExecutableElement[] getInitElements();
	
	ExecutableElement[] getDestroyElements();
	
	ModuleBeanSocketInfo[] getSockets();
	
	ModuleBeanSocketInfo[] getRequiredSockets();
	
	ModuleBeanSocketInfo[] getOptionalSockets();
}
