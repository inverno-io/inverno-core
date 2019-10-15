@io.winterframework.core.annotation.Module

module io.winterframework.test.error.moduleI {
	requires io.winterframework.core;
	requires io.winterframework.core.annotation;
	
	requires io.winterframework.test.error.moduleH;
	
	exports io.winterframework.test.error.moduleI;
}