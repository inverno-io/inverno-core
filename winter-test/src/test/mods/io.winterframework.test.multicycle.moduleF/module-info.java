@io.winterframework.core.annotation.Module
module io.winterframework.test.multicycle.moduleF {
	requires io.winterframework.core;
	requires io.winterframework.core.annotation;
	
	requires io.winterframework.test.multicycle.moduleAPI;
	
	exports io.winterframework.test.multicycle.moduleF;
}