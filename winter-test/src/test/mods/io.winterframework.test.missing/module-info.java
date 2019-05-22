@io.winterframework.core.annotation.Module
module io.winterframework.test.missing {
	
	requires java.sql;
	requires io.winterframework.core;
	requires io.winterframework.core.annotation;
	
	exports io.winterframework.test.missing;
}