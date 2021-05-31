/*
 * Copyright 2019 Jeremy KUHN
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
@io.inverno.core.annotation.Module

@io.inverno.core.annotation.Wire(beans={"runnable1"}, into="io.inverno.core.test.explicitwire.moduleD:runnableSocket1")
@io.inverno.core.annotation.Wire(beans={"io.inverno.core.test.explicitwire.moduleC:runnable2"}, into="io.inverno.core.test.explicitwire.moduleD:runnableSocket2")
@io.inverno.core.annotation.Wire(beans={"io.inverno.core.test.explicitwire.moduleC:runnable1","io.inverno.core.test.explicitwire.moduleC:runnable3"}, into="io.inverno.core.test.explicitwire.moduleD:runnablesSocket")

@io.inverno.core.annotation.Wire(beans={"io.inverno.core.test.explicitwire.moduleD:callableA"}, into="beanC:callable1")
@io.inverno.core.annotation.Wire(beans={"io.inverno.core.test.explicitwire.moduleD:callableB"}, into="io.inverno.core.test.explicitwire.moduleC:beanC:callable2")
@io.inverno.core.annotation.Wire(beans={"io.inverno.core.test.explicitwire.moduleD:callableA","io.inverno.core.test.explicitwire.moduleD:callableC"}, into="io.inverno.core.test.explicitwire.moduleC:beanC:callables")
module io.inverno.core.test.explicitwire.moduleC {
	requires io.inverno.core;
	requires io.inverno.core.annotation;
	
	requires io.inverno.core.test.explicitwire.moduleD; 
	
	exports io.inverno.core.test.explicitwire.moduleC;
}
