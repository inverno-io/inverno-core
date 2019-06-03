@io.winterframework.core.annotation.Module
module io.winterframework.test.multicycle.moduleA {
	requires io.winterframework.core;
	requires io.winterframework.core.annotation;
	
	requires io.winterframework.test.multicycle.moduleB;
	
	exports io.winterframework.test.multicycle.moduleA;
}