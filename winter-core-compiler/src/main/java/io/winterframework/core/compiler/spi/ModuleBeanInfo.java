/**
 * 
 */
package io.winterframework.core.compiler.spi;

import javax.lang.model.element.ExecutableElement;

import io.winterframework.core.annotation.Bean;
import io.winterframework.core.annotation.Scope;

/**
 * @author jkuhn
 *
 */
public interface ModuleBeanInfo extends BeanInfo {

	Scope.Type getScope();
	
	Bean.Visibility getVisibility();
	
	ExecutableElement[] getInitElements();
	
	ExecutableElement[] getDestroyElements();
	
	ModuleBeanSocketInfo[] getSockets();
	
	ModuleBeanSocketInfo[] getRequiredSockets();
	
	ModuleBeanSocketInfo[] getOptionalSockets();
}
