@io.winterframework.core.annotation.Module

@io.winterframework.core.annotation.Wire(beans={"service1"}, into="beanA:service")
@io.winterframework.core.annotation.Wire(beans={"io.winterframework.test.manualwire:service2"}, into="io.winterframework.test.manualwire:beanB:service")
@io.winterframework.core.annotation.Wire(beans={"io.winterframework.test.manualwire:service1","io.winterframework.test.manualwire:service3"}, into="io.winterframework.test.manualwire:beanC:services")
module io.winterframework.test.manualwire {
	requires io.winterframework.core;
	requires io.winterframework.core.annotation;
	
	exports io.winterframework.test.manualwire;
}