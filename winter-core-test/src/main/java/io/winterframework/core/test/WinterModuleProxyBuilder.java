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
package io.winterframework.core.test;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * @author jkuhn
 *
 */
public class WinterModuleProxyBuilder {

	private String moduleName;
	
	private Class<?> moduleBuilderClass;
	
	private Supplier<Object> moduleBuilderSupplier;

	private List<Consumer<Object>> moduleOptionalSetters;
	
	public WinterModuleProxyBuilder(String moduleName, Class<?> moduleBuilderClass) {
		this.moduleName = moduleName;
		this.moduleBuilderClass = moduleBuilderClass;
		
		this.moduleOptionalSetters = new ArrayList<>();
	}
	
	public WinterModuleProxyBuilder dependencies(Object... values) {
		final Constructor<?> moduleBuilderConstructor;
		try {
			Constructor<?>[] constructors = this.moduleBuilderClass.getConstructors();
			if(constructors.length > 1 || constructors[0].getParameterCount() != values.length) {
				throw new RuntimeException("Module Builder does not define constructor public Builder(" + Arrays.stream(values).map(o -> o.getClass().getCanonicalName()).collect(Collectors.joining(", ")) + ")");
			}
			moduleBuilderConstructor = constructors[0];
		}
		catch (SecurityException e) {
			throw new RuntimeException("Module Builder does not define constructor Builder(" + Arrays.stream(values).map(o -> o.getClass().getCanonicalName()).collect(Collectors.joining(", ")) + ")", e);
		}
		
		this.moduleBuilderSupplier = () -> {
			try {
				return moduleBuilderConstructor.newInstance(values);
			}
			catch (InstantiationException | IllegalAccessException | IllegalArgumentException	| InvocationTargetException e) {
				throw new RuntimeException("Error instantiating builder for module " + this.moduleName, e);
			}
		};
		return this;
	}
	
	public WinterModuleProxyBuilder optionalDependency(String name, Object value) {
		
		Optional<Method> depSetterOptional = Arrays.stream(this.moduleBuilderClass.getMethods()).filter(m -> m.getName().equals("set" + Character.toUpperCase(name.charAt(0)) + name.substring(1))).findFirst();
		if(!depSetterOptional.isPresent()) {
			throw new IllegalArgumentException("No dependency " + name + " exists on module " + this.moduleName);
		}
		
		final Method depSetter = depSetterOptional.get();
		
		this.moduleOptionalSetters.add(moduleBuilder -> {
			try {
				depSetter.invoke(moduleBuilder, value);
			} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
				throw new RuntimeException("Error setting optional dependency " + name + " on module " + this.moduleName, e);
			}
		});
		
		return this;
	}
	
	public WinterModuleProxy build() {
		if(this.moduleBuilderSupplier == null) {
			final Constructor<?> moduleBuilderConstructor;
			try {
				moduleBuilderConstructor = this.moduleBuilderClass.getConstructor();
			}
			catch (NoSuchMethodException | SecurityException e) {
				throw new RuntimeException("Module Builder does not define constructor Builder()", e);
			}
			
			this.moduleBuilderSupplier = () -> {
				try {
					return moduleBuilderConstructor.newInstance();
				} catch (InstantiationException | IllegalAccessException | IllegalArgumentException	| InvocationTargetException e) {
					throw new RuntimeException("Error instantiating builder for module " + this.moduleName, e);
				}
			};
		}
		
		Object moduleBuilder = this.moduleBuilderSupplier.get();
		for(Consumer<Object> setter : this.moduleOptionalSetters) {
			setter.accept(moduleBuilder);
		}
		
		try {
			return new WinterModuleProxy(this.moduleBuilderClass.getMethod("build").invoke(moduleBuilder));
		} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException
				| SecurityException e) {
			throw new RuntimeException("Error building proxy for module " + this.moduleName, e);
		}
	}
}
