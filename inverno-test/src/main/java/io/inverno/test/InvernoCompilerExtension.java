/*
 * Copyright 2024 Jeremy Kuhn
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
package io.inverno.test;

import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.TestInstancePostProcessor;

/**
 * <p>
 * Used to inject an {@link InvernoTestCompiler} instance in the test instance.
 * </p>
 * 
 * <p>
 * This extension requires the test class to implement {@link InvernoCompilerTest} to be able to create the compiler instance using {@link InvernoCompilerTest#getModuleOverride()} and 
 * {@link InvernoCompilerTest#getAnnotationProcessorModuleOverride() } and to inject it using {@link InvernoCompilerTest#setInvernoCompiler(io.inverno.test.InvernoTestCompiler) }.
 * </p>
 * 
 * @author <a href="jeremy.kuhn@inverno.io">Jeremy Kuhn</a>
 */
public class InvernoCompilerExtension implements TestInstancePostProcessor {

	@Override
	public void postProcessTestInstance(Object o, ExtensionContext ec) throws Exception {
		if(o instanceof InvernoCompilerTest) {
			InvernoCompilerTest invernoCompilerTest = (InvernoCompilerTest)o;
			
			invernoCompilerTest.setInvernoCompiler(InvernoTestCompiler.builder()
				.moduleOverride(invernoCompilerTest.getModuleOverride())
				.annotationProcessorModuleOverride(invernoCompilerTest.getAnnotationProcessorModuleOverride())
				.build()
			);
		}
	}
}
