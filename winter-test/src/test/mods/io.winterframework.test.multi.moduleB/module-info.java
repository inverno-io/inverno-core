@io.winterframework.core.annotation.Module
module io.winterframework.test.multi.moduleB {
	requires io.winterframework.core;
	requires io.winterframework.core.annotation;
	
	requires io.winterframework.test.multi.moduleA;
	
	exports io.winterframework.test.multi.moduleB;
}