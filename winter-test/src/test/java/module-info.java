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
 * Winter framework unit tests module.
 * </p>
 * 
 * @author jkuhn
 *
 */
module io.winterframework.test {
	requires java.compiler;
	
	requires java.sql;
	requires jdk.httpserver;
	requires io.winterframework.core;
	requires io.winterframework.core.compiler;
	requires io.winterframework.core.annotation;
	requires io.winterframework.core.test;
	
	requires org.junit.jupiter.api;
	requires org.junit.platform.launcher;
	requires org.junit.platform.commons;
	requires org.junit.jupiter.engine;
	requires org.mockito;
}