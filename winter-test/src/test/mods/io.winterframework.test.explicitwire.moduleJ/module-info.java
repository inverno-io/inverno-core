@io.winterframework.core.annotation.Module

@io.winterframework.core.annotation.Wire(beans={"runnable1", "runnable2"}, into="beanJ:runnable")
module io.winterframework.test.explicitwire.moduleJ {
	requires io.winterframework.core;
	requires io.winterframework.core.annotation;
	
	exports io.winterframework.test.explicitwire.moduleJ;
}