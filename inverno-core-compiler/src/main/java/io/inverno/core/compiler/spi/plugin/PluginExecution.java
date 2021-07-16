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
package io.inverno.core.compiler.spi.plugin;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.nio.file.Path;
import java.util.Set;
import java.util.function.Supplier;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.ModuleElement;
import javax.lang.model.element.TypeElement;

import io.inverno.core.annotation.Module;
import io.inverno.core.compiler.spi.BeanInfo;
import io.inverno.core.compiler.spi.ModuleQualifiedName;
import io.inverno.core.compiler.spi.ReporterInfo;

/**
 * <p>
 * A plugin execution is provided during the execution of a
 * {@link CompilerPlugin} and give access to the module beans and the program
 * elements claimed by the plugin (see
 * {@link CompilerPlugin#getSupportedAnnotationTypes()}).
 * </p>
 * 
 * <p>
 * It also allows to create source file or resource file.
 * </p>
 * 
 * @author <a href="mailto:jeremy.kuhn@inverno.io">Jeremy Kuhn</a>
 * @since 1.1
 */
public interface PluginExecution {

	/**
	 * <p>
	 * Returns the element of the module being compiled.
	 * </p>
	 * 
	 * @return a module element
	 */
	ModuleElement getModuleElement();
	
	/**
	 * <p>
	 * Returns the qualified name of the module being compiled.
	 * </p>
	 * 
	 * @return a module qualified name
	 */
	ModuleQualifiedName getModuleQualifiedName();
	
	/**
	 * <p>
	 * Returns the program elements claimed by the plugin.
	 * </p>
	 * 
	 * @param <T> the expected type of the elements.
	 * 
	 * @return a list of elements
	 */
	<T extends Element> Set<T> getElements();
	
	/**
	 * <p>
	 * Returns the program elements claimed by the plugin and annotated with the
	 * specified annotation.
	 * </p>
	 * 
	 * @param <T> the expected type of the elements
	 * @param a   The annotation type
	 * 
	 * @return a list of elements
	 */
	<T extends Element> Set<T> getElementsAnnotatedWith(Class<? extends Annotation> a);
	
	/**
	 * <p>
	 * Returns the program elements claimed by the plugin and annotated with the
	 * specified annotation.
	 * </p>
	 * 
	 * @param <T> the expected type of the elements
	 * @param a   The annotation type element
	 * 
	 * @return a list of elements
	 */
	<T extends Element> Set<T> getElementsAnnotatedWith(TypeElement a);
	
	/**
	 * <p>
	 * Returns the module beans.
	 * </p>
	 * 
	 * <p>
	 * Note that this list doesn't include beans coming from source files generated
	 * by the Inverno compiler or plugins.
	 * </p>
	 * 
	 * @return a list of beans
	 */
	BeanInfo[] getBeans();
	
	/**
	 * <p>
	 * Creates a reporter for the specified element.
	 * </p>
	 * 
	 * @param element the element
	 * 
	 * @return a new reporter
	 */
	ReporterInfo getReporter(Element element);
	
	/**
	 * <p>
	 * Creates a reporter for the specified element and annotation.
	 * </p>
	 * 
	 * @param element    the element
	 * @param annotation the annotation
	 * 
	 * @return a new reporter
	 */
	ReporterInfo getReporter(Element element, AnnotationMirror annotation);
	
	/**
	 * <p>
	 * Creates a source file in the module's source package (see
	 * {@link Module#sourcePackage()}.
	 * </p>
	 * 
	 * @param name                the source file name
	 * @param originatingElements the originating elements
	 * @param source              the source content supplier
	 * 
	 * @throws IOException If an I/O error occurs during the creation of the file
	 */
	void createSourceFile(String name, Element[] originatingElements, Supplier<String> source) throws IOException;
	
	/**
	 * <p>
	 * Creates a resource file in the module.
	 * </p>
	 * 
	 * @param path                the path in the module where to create the file
	 * @param originatingElements the originating elements
	 * @param resource            the resource content supplier
	 * 
	 * @throws IOException If an I/O error occurs during the creation of the file
	 */
	void createResourceFile(String path, Element[] originatingElements, Supplier<String> resource) throws IOException;
	
	/**
	 * <p>
	 * Returns the path to the module source directory.
	 * </p>
	 * 
	 * @return The path to the source directory
	 * @throws IOException If an I/O error occurs
	 */
	Path getModuleSourceDir() throws IOException;
}
