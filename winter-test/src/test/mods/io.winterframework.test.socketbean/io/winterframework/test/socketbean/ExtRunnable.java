/**
 * 
 */
package io.winterframework.test.socketbean;

import java.util.function.Supplier;

import io.winterframework.core.annotation.Bean;

/**
 * @author jkuhn
 *
 */
@Bean
public interface ExtRunnable extends Supplier<Runnable> {

}
