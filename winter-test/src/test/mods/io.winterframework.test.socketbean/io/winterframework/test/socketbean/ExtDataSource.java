/**
 * 
 */
package io.winterframework.test.socketbean;

import java.util.function.Supplier;

import javax.sql.DataSource;

import io.winterframework.core.annotation.Bean;

/**
 * @author jkuhn
 *
 */
@Bean
public interface ExtDataSource extends Supplier<DataSource> {

}
