@io.winterframework.core.annotation.Module

@io.winterframework.core.annotation.Wire(beans={"runnable1", "runnable1"}, into="beanH:runnables")
module io.winterframework.test.explicitwire.moduleH {
	requires io.winterframework.core;
	requires io.winterframework.core.annotation;
	
	exports io.winterframework.test.explicitwire.moduleH;
}