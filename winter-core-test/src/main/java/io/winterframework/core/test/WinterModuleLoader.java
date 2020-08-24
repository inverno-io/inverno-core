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

import java.lang.annotation.Annotation;
import java.lang.module.Configuration;
import java.lang.module.ModuleFinder;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collection;
import java.util.Optional;

/**
 * @author jkuhn
 *
 */
public class WinterModuleLoader {
	
	private ModuleLayer layer;
	
	public WinterModuleLoader(Collection<Path> modulePaths, Collection<String> modules) throws MalformedURLException {
		ModuleFinder finder = ModuleFinder.of(modulePaths.toArray(new Path[modulePaths.size()]));
		ModuleLayer parent = ModuleLayer.boot();
		Configuration cf = parent.configuration().resolve(finder, ModuleFinder.of(), modules);
		
		this.layer = parent.defineModulesWithOneLoader(cf, ClassLoader.getPlatformClassLoader());
	}
	
	public Class<?> loadClass(String moduleName, String className) throws ClassNotFoundException {
		return this.layer.findLoader(moduleName).loadClass(className);
	}
	
	public WinterModuleProxyBuilder load(String moduleName) {
		Optional<Module> module = this.layer.findModule(moduleName);
		if(module.isPresent()) {
			Optional<Annotation> moduleAnnotation = Arrays.stream(module.get().getAnnotations())
				.filter(a -> a.annotationType().getCanonicalName().equals(io.winterframework.core.annotation.Module.class.getCanonicalName()))
				.findFirst();
			
			if(moduleAnnotation.isPresent()) {
				String moduleClassName = null;
				try {
					moduleClassName = (String)moduleAnnotation.get().getClass().getMethod("className").invoke(moduleAnnotation.get());
				} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException
						| NoSuchMethodException | SecurityException e) {
					e.printStackTrace();
				}
				if(moduleClassName == null || moduleClassName.equals("")) {
					String[] moduleNameParts = moduleName.split("\\.");
					moduleClassName = moduleNameParts[moduleNameParts.length-1];
					moduleClassName = moduleName + "." + Character.toUpperCase(moduleClassName.charAt(0)) + moduleClassName.substring(1);
				}
				
				try {
					Class<?> moduleBuilderClass = layer.findLoader(moduleName).loadClass(moduleClassName + "$Builder");
					return new WinterModuleProxyBuilder(moduleName, moduleBuilderClass);
					
				} catch (ClassNotFoundException e) {
					throw new RuntimeException("Error loading module " +  moduleName);
				}
			}
			else {
				throw new RuntimeException("Module " + moduleName + " is not a winter module.");
			}
		}
		else {
			throw new RuntimeException("Unkown module " + moduleName);
		}
	}
}
