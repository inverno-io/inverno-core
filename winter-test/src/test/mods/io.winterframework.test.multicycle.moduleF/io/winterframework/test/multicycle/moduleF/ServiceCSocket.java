package io.winterframework.test.multicycle.moduleF;

import io.winterframework.test.multicycle.moduleAPI.ServiceC;
import io.winterframework.core.annotation.Bean;
import java.util.function.Supplier;

@Bean
public interface ServiceCSocket extends Supplier<ServiceC> {
	
}
