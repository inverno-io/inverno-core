/*
 * Copyright 2018 Jeremy KUHN
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/**
 * <p>
 * The Winter framework compiler module, providing a {@link Module} annotation
 * processor that analyzes the source code looking for {@link Bean} annotated
 * class to generate a corresponding module class at compile time.
 * </p>
 * 
 * @author jkuhn
 *
 * @provides javax.annotation.processing.Processor
 */
module io.winterframework.core.compiler {
	requires transitive java.compiler;
	requires transitive jdk.compiler;
	requires transitive io.winterframework.core.annotation;
	
	exports io.winterframework.core.compiler.spi;
	exports io.winterframework.core.compiler.spi.plugin;
	exports io.winterframework.core.compiler.spi.support;
	
	provides javax.annotation.processing.Processor with io.winterframework.core.compiler.WinterCompiler;
	
	uses io.winterframework.core.compiler.spi.plugin.CompilerPlugin;
}
