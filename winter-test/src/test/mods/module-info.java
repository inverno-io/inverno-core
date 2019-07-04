@io.winterframework.core.annotation.Module
module io.winterframework.test.multicycle.moduleE {
	requires io.winterframework.core;
	requires io.winterframework.core.annotation;
	
	requires io.winterframework.test.multicycle.moduleAPI;
	requires io.winterframework.test.multicycle.moduleF;
	
	exports io.winterframework.test.multicycle.moduleE;
}