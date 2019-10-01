@io.winterframework.core.annotation.Module(excludes={"io.winterframework.test.includeexclude.moduleA"})
module io.winterframework.test.includeexclude.moduleD {
	requires io.winterframework.core;
	requires io.winterframework.core.annotation;
	
	requires io.winterframework.test.includeexclude.moduleA;
	requires io.winterframework.test.includeexclude.moduleB;
	
	exports io.winterframework.test.includeexclude.moduleD;
}