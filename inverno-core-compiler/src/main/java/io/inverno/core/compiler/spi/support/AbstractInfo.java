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
package io.inverno.core.compiler.spi.support;

import java.util.Objects;

import io.inverno.core.compiler.spi.Info;
import io.inverno.core.compiler.spi.QualifiedName;
import io.inverno.core.compiler.spi.ReporterInfo;

/**
 * <p>Base {@link Info} implementation.</p>
 * 
 * @author <a href="mailto:jeremy.kuhn@inverno.io">Jeremy Kuhn</a>
 */
public abstract class AbstractInfo<E extends QualifiedName> implements Info {
	
	private final E name;
	
	private final ReporterInfo reporter;
	
	/**
	 * <p>
	 * Creates an info.
	 * </p>
	 * 
	 * @param name     the qualified name
	 * @param reporter the reporter
	 */
	public AbstractInfo(E name, ReporterInfo reporter) {
		this.name = Objects.requireNonNull(name);
		this.reporter = Objects.requireNonNull(reporter);
	}

	@Override
	public boolean hasError() {
		return this.reporter.hasError();
	}

	@Override
	public boolean hasWarning() {
		return this.reporter.hasWarning();
	}

	@Override
	public void error(String message) {
		this.reporter.error(message);
	}

	@Override
	public void warning(String message) {
		this.reporter.warning(message);
	}

	@Override
	public E getQualifiedName() {
		return this.name;
	}
}
