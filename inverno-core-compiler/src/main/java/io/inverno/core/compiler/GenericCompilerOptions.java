/*
 * Copyright 2021 Jeremy KUHN
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
package io.inverno.core.compiler;

import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;

import io.inverno.core.compiler.spi.CompilerOptions;

/**
 * @author <a href="mailto:jeremy.kuhn@inverno.io">Jeremy Kuhn</a>
 *
 */
public class GenericCompilerOptions implements CompilerOptions {

	public static final String DEBUG = "inverno.debug";
	public static final String VERBOSE = "inverno.verbose";
	public static final String GENERATE_DESCRIPTOR = "inverno.generateDescriptor";
	
	private Predicate<String> nameFilter;
	private Map<String, String> processingEnvOptions;
	
	private boolean debug;
	private boolean verbose;
	private boolean generateModuleDescriptor;
	
	public GenericCompilerOptions(Map<String, String> processingEnvOptions) {
		this.processingEnvOptions = processingEnvOptions;
		this.nameFilter = name -> true;
		this.debug = processingEnvOptions.containsKey(DEBUG) && (processingEnvOptions.get(DEBUG) == null || Boolean.valueOf(processingEnvOptions.get(DEBUG)));
		this.verbose = processingEnvOptions.containsKey(VERBOSE) && (processingEnvOptions.get(VERBOSE) == null || Boolean.valueOf(processingEnvOptions.get(VERBOSE)));
		this.generateModuleDescriptor = processingEnvOptions.containsKey(GENERATE_DESCRIPTOR) && (processingEnvOptions.get(GENERATE_DESCRIPTOR) == null || Boolean.valueOf(processingEnvOptions.get(GENERATE_DESCRIPTOR)));
	}
	
	private GenericCompilerOptions(GenericCompilerOptions parentOptions, Predicate<String> nameFilter) {
		this.processingEnvOptions = parentOptions.processingEnvOptions;
		this.nameFilter = nameFilter;
		this.debug = parentOptions.debug;
		this.verbose = parentOptions.verbose;
		this.generateModuleDescriptor = parentOptions.generateModuleDescriptor;
	}
	
	public GenericCompilerOptions withFilter(Predicate<String> namefilter) {
		return new GenericCompilerOptions(this, namefilter);
	}

	public boolean isDebug() {
		return debug;
	}

	public boolean isVerbose() {
		return verbose;
	}
	
	public boolean isGenerateModuleDescriptor() {
		return generateModuleDescriptor;
	}

	@Override
	public boolean containsOption(String name) {
		if(this.nameFilter.test(name)) {
			return this.processingEnvOptions.containsKey(name);
		}
		return false;
	}
	
	@Override
	public Optional<String> getOption(String name) {
		if(this.nameFilter.test(name)) {
			return Optional.ofNullable(this.processingEnvOptions.get(name));
		}
		return Optional.empty();
	}
	
	@Override
	public boolean isOptionActivated(String name, boolean defaultActivation) {
		if(this.nameFilter.test(name)) {
			if(!this.processingEnvOptions.containsKey(name)) {
				return defaultActivation;
			}
			else {
				return this.getOption(name).map(Boolean::valueOf).orElse(true);
			}
		}
		return defaultActivation;
	}
}
