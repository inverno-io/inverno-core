/*
 * Copyright 2020 Jeremy KUHN
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
package io.inverno.core.test;

import java.util.Optional;
import java.util.function.Function;

import io.inverno.test.InvernoCompilerExtension;
import io.inverno.test.InvernoCompilerTest;
import io.inverno.test.InvernoTestCompiler;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.extension.ExtendWith;

/**
 * @author <a href="mailto:jeremy.kuhn@inverno.io">Jeremy Kuhn</a>
 */
@ExtendWith(InvernoCompilerExtension.class)
public class AbstractCoreInvernoTest implements InvernoCompilerTest {
	
	private InvernoTestCompiler invernoCompiler;

	@Override
	public void setInvernoCompiler(InvernoTestCompiler invernoCompiler) {
		this.invernoCompiler = invernoCompiler;
	}

	public InvernoTestCompiler getInvernoCompiler() {
		return invernoCompiler;
	}

	@Override
	public Function<Path, Path> getModuleOverride() {
		return path -> {
			if(Files.exists(Path.of("../inverno-test"))) {
				if(path.getFileName().toString().startsWith("inverno-core-annotation")) {
					return Optional.of(Path.of("../inverno-core-annotation/target/classes")).filter(Files::exists).orElse(path);
				}
				else if(path.getFileName().toString().startsWith("inverno-core-compiler")) {
					return null;
				}
				else if(path.getFileName().toString().startsWith("inverno-core")) {
					return Optional.of(Path.of("../inverno-core/target/classes")).filter(Files::exists).orElse(path);
				}
			}
			return path;
		};
	}

	@Override
	public Function<Path, Path> getAnnotationProcessorModuleOverride() {
		return path -> {
			if(path.getFileName().toString().startsWith("inverno-core-annotation")) {
				return Optional.of(Path.of("../inverno-core-annotation/target/classes")).filter(Files::exists).orElse(path);
			}
			else if(path.getFileName().toString().startsWith("inverno-core-compiler")) {
				return Optional.of(Path.of("../inverno-core-compiler/target/classes")).filter(Files::exists).orElse(path);
			}
			return null;
		};
	}
}
