@io.winterframework.core.annotation.Module
module io.winterframework.test.multi.moduleC {
	requires io.winterframework.core;
	requires io.winterframework.core.annotation;
	
	requires io.winterframework.test.multi.moduleA;
	requires io.winterframework.test.multi.moduleB;
	
	exports io.winterframework.test.multi.moduleC;
}