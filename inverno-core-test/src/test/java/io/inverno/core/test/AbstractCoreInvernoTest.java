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

import java.io.File;
import java.util.Optional;
import java.util.function.Function;

import io.inverno.test.AbstractInvernoTest;

/**
 * @author <a href="mailto:jeremy.kuhn@inverno.io">Jeremy Kuhn</a>
 *
 */
public class AbstractCoreInvernoTest extends AbstractInvernoTest {

	public AbstractCoreInvernoTest() {
		super((Function<File, File>)file -> {
			if(new File("../inverno-test").exists()) {
				if(file.getName().startsWith("inverno-core-annotation")) {
					return Optional.of(new File("../inverno-core-annotation/target/classes")).filter(File::exists).orElse(file);
				}
				else if(file.getName().startsWith("inverno-core-compiler")) {
					return null;
				}
				else if(file.getName().startsWith("inverno-core")) {
					return Optional.of(new File("../inverno-core/target/classes")).filter(File::exists).orElse(file);
				}
			}
			return file;
		},
		file -> {
			if(file.getName().startsWith("inverno-core-annotation")) {
				return Optional.of(new File("../inverno-core-annotation/target/classes")).filter(File::exists).orElse(file);
			}
			else if(file.getName().startsWith("inverno-core-compiler")) {
				return Optional.of(new File("../inverno-core-compiler/target/classes")).filter(File::exists).orElse(file);
			}
			return null;
		});
	}
}
