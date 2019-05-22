@io.winterframework.core.annotation.Module
module io.winterframework.test.socketbean {
	
	requires transitive java.sql;
	requires transitive jdk.httpserver;
	requires io.winterframework.core;
	requires io.winterframework.core.annotation;
	
	exports io.winterframework.test.socketbean;
}