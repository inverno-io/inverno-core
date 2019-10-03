@io.winterframework.core.annotation.Module

@io.winterframework.core.annotation.Wire(beans={"runnable1"}, into="io.winterframework.test.explicitwire.moduleD:runnableSocket1")
@io.winterframework.core.annotation.Wire(beans={"io.winterframework.test.explicitwire.moduleC:runnable2"}, into="io.winterframework.test.explicitwire.moduleD:runnableSocket2")
@io.winterframework.core.annotation.Wire(beans={"io.winterframework.test.explicitwire.moduleC:runnable1","io.winterframework.test.explicitwire.moduleC:runnable3"}, into="io.winterframework.test.explicitwire.moduleD:runnablesSocket")

@io.winterframework.core.annotation.Wire(beans={"io.winterframework.test.explicitwire.moduleD:callableA"}, into="beanC:callable1")
@io.winterframework.core.annotation.Wire(beans={"io.winterframework.test.explicitwire.moduleD:callableB"}, into="io.winterframework.test.explicitwire.moduleC:beanC:callable2")
@io.winterframework.core.annotation.Wire(beans={"io.winterframework.test.explicitwire.moduleD:callableA","io.winterframework.test.explicitwire.moduleD:callableC"}, into="io.winterframework.test.explicitwire.moduleC:beanC:callables")
module io.winterframework.test.explicitwire.moduleC {
	requires io.winterframework.core;
	requires io.winterframework.core.annotation;
	
	requires io.winterframework.test.explicitwire.moduleD; 
	
	exports io.winterframework.test.explicitwire.moduleC;
}