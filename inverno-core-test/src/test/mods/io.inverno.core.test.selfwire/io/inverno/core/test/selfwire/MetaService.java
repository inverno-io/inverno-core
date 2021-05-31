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
package io.inverno.core.test.selfwire;

import java.util.Set;
import java.util.stream.Collectors;

import io.inverno.core.annotation.Bean;

@Bean
public class MetaService implements Service {

	public Set<Service> services;
	
	public MetaService(Set<Service> services) {
		this.services = services;
	}
	
	public String execute() {
		return this.services.stream().map(Service::execute).collect(Collectors.joining(","));
	}
}
