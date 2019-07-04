@io.winterframework.core.annotation.Module
module io.winterframework.test.multicycle.moduleC {
	requires io.winterframework.core;
	requires io.winterframework.core.annotation;
	
	requires io.winterframework.test.multicycle.moduleAPI;
	requires io.winterframework.test.multicycle.moduleD;
	requires io.winterframework.test.multicycle.moduleE;
	
	exports io.winterframework.test.multicycle.moduleC;
}