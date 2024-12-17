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
package io.inverno.test;

import java.lang.annotation.Annotation;
import java.lang.module.Configuration;
import java.lang.module.ModuleFinder;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import javax.tools.Diagnostic;
import javax.tools.JavaFileObject;

/**
 * @author <a href="mailto:jeremy.kuhn@inverno.io">Jeremy Kuhn</a>
 *
 */
public class InvernoModuleLoader {
	
	private final ModuleLayer layer;
	
	private final List<Diagnostic<? extends JavaFileObject>> diagnotics;
	
	public InvernoModuleLoader(Collection<Path> modulePaths, Collection<String> modules, List<Diagnostic<? extends JavaFileObject>> diagnotics) throws MalformedURLException {
		ModuleFinder finder = ModuleFinder.of(modulePaths.toArray(new Path[modulePaths.size()]));
		ModuleLayer parent = ModuleLayer.boot();
		
		// This is necessary to be able to run junit in Eclipse
		// The issue is related to the fact that the boot module layer contains project dependencies leading to modules conflicts
		// Maven doesn't have this issue as it create a child layer to run junit
		Set<String> parentNames = parent.modules().stream().map(Module::getName).collect(Collectors.toSet());
		finder = ModuleFinder.of(finder.findAll().stream()
			.filter(ref -> !parentNames.contains(ref.descriptor().name()))
			.filter(ref -> ref.location().isPresent())
			.map(ref -> Path.of(ref.location().get()))
			.toArray(Path[]::new));
		
		Configuration cf = parent.configuration().resolve(finder, ModuleFinder.of(), modules);
		
		this.layer = parent.defineModulesWithOneLoader(cf, ClassLoader.getPlatformClassLoader());
		this.diagnotics = diagnotics;
	}
	
	public Class<?> loadClass(String moduleName, String className) throws ClassNotFoundException {
		return this.layer.findLoader(moduleName).loadClass(className);
	}
	
	public InvernoModuleProxyBuilder load(String moduleName) {
		Optional<Module> module = this.layer.findModule(moduleName);
		if(module.isPresent()) {
			Optional<Annotation> moduleAnnotation = Arrays.stream(module.get().getAnnotations())
				.filter(a -> a.annotationType().getCanonicalName().equals(io.inverno.core.annotation.Module.class.getCanonicalName()))
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
					return new InvernoModuleProxyBuilder(moduleName, moduleBuilderClass);
					
				} catch (ClassNotFoundException e) {
					throw new RuntimeException("Error loading module " +  moduleName);
				}
			}
			else {
				throw new RuntimeException("Module " + moduleName + " is not an Inverno module.");
			}
		}
		else {
			throw new RuntimeException("Unkown module " + moduleName);
		}
	}

	public List<Diagnostic<? extends JavaFileObject>> getDiagnotics() {
		return this.diagnotics;
	}
}
