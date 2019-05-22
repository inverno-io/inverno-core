module io.winterframework.core.compiler {
	requires transitive java.compiler;
	requires transitive io.winterframework.core.annotation;
	
	exports io.winterframework.core.compiler;
	exports io.winterframework.core.compiler.spi;
	
	provides javax.annotation.processing.Processor with io.winterframework.core.compiler.ModuleAnnotationProcessor;
}
