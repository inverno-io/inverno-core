@io.winterframework.core.annotation.Module

@io.winterframework.core.annotation.Wire(beans={"runnable1"}, into="beanI:runnables")
@io.winterframework.core.annotation.Wire(beans={"runnable2"}, into="io.winterframework.test.explicitwire.moduleI:beanI:runnables")
module io.winterframework.test.explicitwire.moduleI {
	requires io.winterframework.core;
	requires io.winterframework.core.annotation;
	
	exports io.winterframework.test.explicitwire.moduleI;
}