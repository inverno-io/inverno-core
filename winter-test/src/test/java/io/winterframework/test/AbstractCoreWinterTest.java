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
package io.winterframework.test;

import java.io.File;
import java.util.Optional;
import java.util.function.Function;

import io.winterframework.core.test.AbstractWinterTest;

/**
 * @author jkuhn
 *
 */
public class AbstractCoreWinterTest extends AbstractWinterTest {

	public AbstractCoreWinterTest() {
		super((Function<File, File>)file -> {
			if(new File("../winter-test").exists()) {
				if(file.getName().startsWith("winter-core-annotation")) {
					return Optional.of(new File("../winter-core-annotation/target/classes")).filter(File::exists).orElse(file);
				}
				else if(file.getName().startsWith("winter-core-compiler")) {
					return null;
				}
				else if(file.getName().startsWith("winter-core")) {
					return Optional.of(new File("../winter-core/target/classes")).filter(File::exists).orElse(file);
				}
			}
			return file;
		},
		file -> {
			if(file.getName().startsWith("winter-core-annotation")) {
				return Optional.of(new File("../winter-core-annotation/target/classes")).filter(File::exists).orElse(file);
			}
			else if(file.getName().startsWith("winter-core-compiler")) {
				return Optional.of(new File("../winter-core-compiler/target/classes")).filter(File::exists).orElse(file);
			}
			return null;
		});
	}
}
