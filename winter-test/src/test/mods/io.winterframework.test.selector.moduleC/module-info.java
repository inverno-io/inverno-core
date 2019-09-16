@io.winterframework.core.annotation.Module
module io.winterframework.test.selector.moduleC {
	
	requires io.winterframework.core;
	requires io.winterframework.core.annotation;
	
	requires io.winterframework.test.selector.moduleB;
	
	exports io.winterframework.test.selector.moduleC;
}
