/**
 * 
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
	
	private Class<?> moduleCreatorClass;
	
	private Class<?> moduleBuilderClass;
	
	private Supplier<Object> moduleBuilderSupplier;

	private List<Consumer<Object>> moduleOptionalSetters;
	
	public WinterModuleProxyBuilder(String moduleName, Class<?> moduleCreatorClass, Class<?> moduleBuilderClass) {
		this.moduleName = moduleName;
		this.moduleCreatorClass = moduleCreatorClass;
		this.moduleBuilderClass = moduleBuilderClass;
		
		this.moduleOptionalSetters = new ArrayList<>();
	}
	
	public WinterModuleProxyBuilder dependencies(Object... values) {
		final Constructor<?> moduleCreatorConstructor;
		try {
			moduleCreatorConstructor = this.moduleCreatorClass.getConstructor();
		}
		catch (NoSuchMethodException | SecurityException e) {
			throw new IllegalArgumentException("Module Creator must define an empty-arg constructor.", e);
		}

		Optional<Method> moduleCreatorWithMethodOptional = Arrays.stream(this.moduleCreatorClass.getMethods()).filter(m -> m.getName().equals("with")).findFirst();
		if(!moduleCreatorWithMethodOptional.isPresent()) {
			throw new IllegalArgumentException("Module Creator must define with(" + Arrays.stream(values).map(o -> o.getClass().getCanonicalName()).collect(Collectors.joining(", ")) + ") method.");
		}
		
		final Method moduleCreatorWithMethod = moduleCreatorWithMethodOptional.get();
		
		this.moduleBuilderSupplier = () -> {
			try {
				Object moduleCreator = moduleCreatorConstructor.newInstance();
				return moduleCreatorWithMethod.invoke(moduleCreator, values);
			} catch (InstantiationException | IllegalAccessException | IllegalArgumentException	| InvocationTargetException e) {
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
			final Constructor<?> moduleCreatorConstructor;
			final Method moduleCreatorWithMethod;
			try {
				moduleCreatorConstructor = this.moduleCreatorClass.getConstructor();
				moduleCreatorWithMethod = this.moduleCreatorClass.getMethod("with");
			}
			catch (NoSuchMethodException | SecurityException e) {
				throw new IllegalArgumentException("Module Creator with() is undefined.", e);
			}
			
			this.moduleBuilderSupplier = () -> {
				try {
					Object moduleCreator = moduleCreatorConstructor.newInstance();
					return moduleCreatorWithMethod.invoke(moduleCreator);
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
