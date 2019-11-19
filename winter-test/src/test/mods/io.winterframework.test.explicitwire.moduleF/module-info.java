@io.winterframework.core.annotation.Module

@io.winterframework.core.annotation.Wire(beans={"runnable2"}, into="io.winterframework.test.explicitwire.moduleF:runnableSocket")
module io.winterframework.test.explicitwire.moduleF {
	requires io.winterframework.core;
	requires io.winterframework.core.annotation;
	
	exports io.winterframework.test.explicitwire.moduleF;
}