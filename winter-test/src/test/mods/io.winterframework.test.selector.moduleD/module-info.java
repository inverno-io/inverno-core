@io.winterframework.core.annotation.Module

@io.winterframework.core.annotation.Wire(beans = "runnableB", into = "io.winterframework.test.selector.moduleB:runnableSocket")
module io.winterframework.test.selector.moduleD {
	
	requires io.winterframework.core;
	requires io.winterframework.core.annotation;
	
	requires io.winterframework.test.selector.moduleB;
	
	exports io.winterframework.test.selector.moduleD;
}
