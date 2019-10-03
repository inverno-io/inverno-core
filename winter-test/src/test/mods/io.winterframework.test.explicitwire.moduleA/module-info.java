@io.winterframework.core.annotation.Module

@io.winterframework.core.annotation.Wire(beans={"service1"}, into="beanA:service")
@io.winterframework.core.annotation.Wire(beans={"io.winterframework.test.explicitwire.moduleA:service2"}, into="io.winterframework.test.explicitwire.moduleA:beanB:service")
@io.winterframework.core.annotation.Wire(beans={"io.winterframework.test.explicitwire.moduleA:service1","io.winterframework.test.explicitwire.moduleA:service3"}, into="io.winterframework.test.explicitwire.moduleA:beanC:services")
module io.winterframework.test.explicitwire.moduleA {
	requires io.winterframework.core;
	requires io.winterframework.core.annotation;
	
	exports io.winterframework.test.explicitwire.moduleA;
}