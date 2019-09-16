@io.winterframework.core.annotation.Module

module io.winterframework.test.socketbean.moduleC {
	
	requires io.winterframework.core;
	requires io.winterframework.core.annotation;
	
	requires io.winterframework.test.socketbean.moduleB;
	
	exports io.winterframework.test.socketbean.moduleC;
}
