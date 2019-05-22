@io.winterframework.core.annotation.Module
module io.winterframework.test.multi.moduleD {
	requires io.winterframework.core;
	requires io.winterframework.core.annotation;
	
	requires io.winterframework.test.multi.moduleA;
	requires io.winterframework.test.multi.moduleE;
	requires io.winterframework.test.multi.moduleF;
	
	exports io.winterframework.test.multi.moduleD;
}