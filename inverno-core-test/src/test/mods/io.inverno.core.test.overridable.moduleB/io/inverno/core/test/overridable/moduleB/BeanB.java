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
package io.inverno.core.test.overridable.moduleB;

import io.inverno.core.annotation.Bean;
import io.inverno.core.annotation.Wrapper;
import io.inverno.core.annotation.Overridable;
import io.inverno.core.annotation.BeanSocket;

import java.util.function.Supplier;

@Bean
@Wrapper
@Overridable
public class BeanB implements Supplier<Supplier<String>> {
	
	@Override
	public Supplier<String> get() {
		return () -> "non-overridden";
	}
}
