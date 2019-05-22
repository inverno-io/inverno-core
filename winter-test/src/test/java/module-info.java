module io.winterframework.test {
	requires java.compiler;
	
	requires java.sql;
	requires jdk.httpserver;
	requires io.winterframework.core;
	requires io.winterframework.core.compiler;
	requires io.winterframework.core.annotation;
	requires io.winterframework.core.test;
	
	requires org.junit.jupiter.api;
	requires org.mockito;
}