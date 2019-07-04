package io.winterframework.test.multicycle.moduleD;

import io.winterframework.test.multicycle.moduleAPI.ServiceE;
import io.winterframework.core.annotation.Bean;
import java.util.function.Supplier;

@Bean
public interface ServiceESocket extends Supplier<ServiceE> {
	
}
