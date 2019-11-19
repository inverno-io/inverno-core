@io.winterframework.core.annotation.Module

@io.winterframework.core.annotation.Wire(beans={"service1", "service2"}, into="beanA:service")
@io.winterframework.core.annotation.Wire(beans={"service4", "unkown", "service3"}, into="beanE:services")
module io.winterframework.test.explicitwire.moduleE {
	requires io.winterframework.core;
	requires io.winterframework.core.annotation;
	
	exports io.winterframework.test.explicitwire.moduleE;
}