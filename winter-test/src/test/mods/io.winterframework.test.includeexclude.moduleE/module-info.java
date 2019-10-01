@io.winterframework.core.annotation.Module(includes= {"io.winterframework.test.includeexclude.moduleA", "io.winterframework.test.includeexclude.moduleB"}, excludes={"io.winterframework.test.includeexclude.moduleA"})
module io.winterframework.test.includeexclude.moduleE {
	requires io.winterframework.core;
	requires io.winterframework.core.annotation;
	
	requires io.winterframework.test.includeexclude.moduleA;
	requires io.winterframework.test.includeexclude.moduleB;
	
	exports io.winterframework.test.includeexclude.moduleE;
}