@io.winterframework.core.annotation.Module
module io.winterframework.test.provide.moduleB {
	
	requires io.winterframework.core;
	requires io.winterframework.core.annotation;
	
	requires io.winterframework.test.provide.moduleA;
	
	exports io.winterframework.test.provide.moduleB;
}
