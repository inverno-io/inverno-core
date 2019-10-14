@io.winterframework.core.annotation.Module

module io.winterframework.test.error.moduleG {
	requires io.winterframework.core;
	requires io.winterframework.core.annotation;
	
	exports io.winterframework.test.error.moduleG;
	exports io.winterframework.test.error.moduleG.sub;
}