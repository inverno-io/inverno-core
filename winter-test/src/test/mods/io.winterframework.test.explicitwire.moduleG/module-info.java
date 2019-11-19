@io.winterframework.core.annotation.Module

@io.winterframework.core.annotation.Wire(beans={"runnable2"}, into="test")
@io.winterframework.core.annotation.Wire(beans={"#bad"}, into="beanG:runnable")
module io.winterframework.test.explicitwire.moduleG {
	requires io.winterframework.core;
	requires io.winterframework.core.annotation;
	
	exports io.winterframework.test.explicitwire.moduleG;
}