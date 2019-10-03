@io.winterframework.core.annotation.Module

@io.winterframework.core.annotation.Wire(beans={"callableA"}, into="beanD:callableA")
@io.winterframework.core.annotation.Wire(beans={"callableB"}, into="beanD:callableB")
@io.winterframework.core.annotation.Wire(beans={"callableC"}, into="beanD:callableC")

@io.winterframework.core.annotation.Wire(beans={"runnableSocket1"}, into="beanD:runnable1")
@io.winterframework.core.annotation.Wire(beans={"runnableSocket2"}, into="beanD:runnable2")
@io.winterframework.core.annotation.Wire(beans={"runnablesSocket"}, into="beanD:runnables")
module io.winterframework.test.explicitwire.moduleD {
	requires io.winterframework.core;
	requires io.winterframework.core.annotation;
	
	exports io.winterframework.test.explicitwire.moduleD;
}