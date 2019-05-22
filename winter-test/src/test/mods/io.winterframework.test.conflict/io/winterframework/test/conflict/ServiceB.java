/**
 * 
 */
package io.winterframework.test.conflict;

import io.winterframework.core.annotation.Bean;

/**
 * @author jkuhn
 *
 */
@Bean
public class ServiceB implements Service {

	@Override
	public String execute() {
		return "Execute Service A";
	}

}
