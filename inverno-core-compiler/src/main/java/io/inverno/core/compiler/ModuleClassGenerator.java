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
package io.inverno.core.compiler;

import io.inverno.core.annotation.Bean;
import io.inverno.core.compiler.ModuleClassGenerationContext.GenerationMode;
import io.inverno.core.compiler.spi.BeanInfo;
import io.inverno.core.compiler.spi.ModuleBeanInfo;
import io.inverno.core.compiler.spi.ModuleBeanMultiSocketInfo;
import io.inverno.core.compiler.spi.ModuleBeanSingleSocketInfo;
import io.inverno.core.compiler.spi.ModuleBeanSocketInfo;
import io.inverno.core.compiler.spi.ModuleInfo;
import io.inverno.core.compiler.spi.ModuleInfoVisitor;
import io.inverno.core.compiler.spi.MultiSocketBeanInfo;
import io.inverno.core.compiler.spi.MultiSocketInfo;
import io.inverno.core.compiler.spi.MutatorBeanInfo;
import io.inverno.core.compiler.spi.NestedBeanInfo;
import io.inverno.core.compiler.spi.OverridableBeanInfo;
import io.inverno.core.compiler.spi.OverridingSocketBeanInfo;
import io.inverno.core.compiler.spi.SingleSocketBeanInfo;
import io.inverno.core.compiler.spi.SingleSocketInfo;
import io.inverno.core.compiler.spi.SocketBeanInfo;
import io.inverno.core.compiler.spi.SocketInfo;
import io.inverno.core.compiler.spi.WrapperBeanInfo;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.type.WildcardType;

/**
 * <p>
 * A {@link ModuleInfoVisitor} implementation used to generates an Inverno module class.
 * </p>
 * 
 * @author <a href="mailto:jeremy.kuhn@inverno.io">Jeremy Kuhn</a>
 */
class ModuleClassGenerator implements ModuleInfoVisitor<StringBuilder, ModuleClassGenerationContext> {

	private static final String INVERNO_CORE_PACKAGE = "io.inverno.core.v1";
	
	private static final String INVERNO_CORE_MODULE_CLASS = INVERNO_CORE_PACKAGE + ".Module";
	private static final String INVERNO_CORE_MODULE_MODULEBUILDER_CLASS = INVERNO_CORE_PACKAGE + ".Module.ModuleBuilder";
	private static final String INVERNO_CORE_MODULE_LINKER_CLASS = INVERNO_CORE_PACKAGE + ".Module.ModuleLinker";
	private static final String INVERNO_CORE_MODULE_BEAN_CLASS = INVERNO_CORE_PACKAGE + ".Module.Bean";
	private static final String INVERNO_CORE_MODULE_WRAPPERBEANBUILDER_CLASS = INVERNO_CORE_PACKAGE + ".Module.WrapperBeanBuilder";
	private static final String INVERNO_CORE_MODULE_MODULEBEANBUILDER_CLASS = INVERNO_CORE_PACKAGE + ".Module.ModuleBeanBuilder";
	private static final String INVERNO_CORE_MODULE_BEANAGGREGATOR_CLASS = INVERNO_CORE_PACKAGE + ".Module.BeanAggregator";
	private static final String INVERNO_CORE_MODULE_SOCKET_ANNOTATION = INVERNO_CORE_PACKAGE + ".Module.Socket";
	
	@Override
	public StringBuilder visit(ModuleInfo moduleInfo, ModuleClassGenerationContext context) {
		String className = moduleInfo.getQualifiedName().getClassName();
		String packageName = className.lastIndexOf(".") != -1 ? className.substring(0, className.lastIndexOf(".")) : "";
		className = className.substring(packageName.length() + 1);
		
		if(context.getMode() == GenerationMode.MODULE_CLASS) {
			TypeMirror generatedType = context.getElementUtils().getTypeElement(context.getElementUtils().getModuleElement("java.compiler"), "javax.annotation.processing.Generated").asType();
			TypeMirror moduleType = context.getElementUtils().getTypeElement(INVERNO_CORE_MODULE_CLASS).asType();

			context.addImport(className, moduleInfo.getQualifiedName().getClassName());
			context.addImport("Builder", moduleInfo.getQualifiedName().getClassName() + ".Builder");
			
			// Fields
			StringBuilder module_field_beans = Arrays.stream(moduleInfo.getBeans())
				.filter(moduleBeanInfo -> !(moduleBeanInfo instanceof MutatorBeanInfo) || ((MutatorBeanInfo)moduleBeanInfo).getMutatingSocket().isWired())
				.map(moduleBeanInfo -> this.visit(moduleBeanInfo, context.withModule(moduleInfo.getQualifiedName()).withMode(GenerationMode.BEAN_FIELD)))
				.collect(context.joining(System.lineSeparator()));
			StringBuilder module_field_modules = Arrays.stream(moduleInfo.getModules())
				.map(componentModuleInfo -> this.visit(componentModuleInfo, context.withModule(moduleInfo.getQualifiedName()).withMode(GenerationMode.COMPONENT_MODULE_FIELD)))
				.collect(context.joining(System.lineSeparator()));
			
			StringBuilder module_constructor_parameters = Arrays.stream(moduleInfo.getSockets()) 
				.filter(socketInfo -> socketInfo.isWired())
				.map(socketInfo -> new StringBuilder().append(context.indent(3)).append(this.visit(socketInfo , context.withModule(moduleInfo.getQualifiedName()).withMode(GenerationMode.SOCKET_PARAMETER))))
				.collect(context.joining("," + System.lineSeparator()));
			
			StringBuilder module_constructor_modules = Arrays.stream(moduleInfo.getModules())
				.map(componentModuleInfo -> this.visit(componentModuleInfo, context.withModule(moduleInfo.getQualifiedName()).withMode(GenerationMode.COMPONENT_MODULE_NEW)))
				.collect(context.joining(System.lineSeparator()));
			
			StringBuilder module_constructor_beans = Arrays.stream(moduleInfo.getBeans())
				.filter(moduleBeanInfo -> !(moduleBeanInfo instanceof MutatorBeanInfo) || ((MutatorBeanInfo)moduleBeanInfo).getMutatingSocket().isWired())
				.map(moduleBeanInfo -> this.visit(moduleBeanInfo, context.withModule(moduleInfo.getQualifiedName()).withMode(GenerationMode.BEAN_NEW)))
				.collect(context.joining(System.lineSeparator()));
			
			StringBuilder module_method_beans = Arrays.stream(moduleInfo.getBeans())
				.filter(moduleBeanInfo -> !(moduleBeanInfo instanceof MutatorBeanInfo) || ((MutatorBeanInfo)moduleBeanInfo).getMutatingSocket().isWired())
				.map(moduleBeanInfo -> this.visit(moduleBeanInfo, context.withModule(moduleInfo.getQualifiedName()).withMode(GenerationMode.BEAN_ACCESSOR)))
				.collect(context.joining(System.lineSeparator()));
			
			StringBuilder module_builder = this.visit(moduleInfo, context.withModule(moduleInfo.getQualifiedName()).withMode(GenerationMode.MODULE_BUILDER_CLASS));
			StringBuilder module_linker = this.visit(moduleInfo, context.withModule(moduleInfo.getQualifiedName()).withMode(GenerationMode.MODULE_LINKER_CLASS));
			
			StringBuilder moduleClass = new StringBuilder();

			if(!packageName.equals("")) {
				moduleClass.append("package ").append(packageName).append(";").append(System.lineSeparator()).append(System.lineSeparator());
			}
			
			context.removeImport(className);
			context.removeImport("Builder");
			context.removeImport("ModuleBuilder");
			context.removeImport("ModuleLinker");
			context.removeImport("Bean");
			context.removeImport("WrapperBeanBuilder");
			context.removeImport("ModuleBeanBuilder");
			context.removeImport("BeanAggregator");
			context.removeImport("Socket");
			
			context.getTypeName(generatedType);
			context.getTypeName(moduleType);
			
			moduleClass.append(context.getImports().stream().sorted().filter(i -> !i.startsWith("java.lang") && i.lastIndexOf(".") > 0 && !i.substring(0, i.lastIndexOf(".")).equals(packageName)).map(i -> new StringBuilder().append("import ").append(i).append(";")).collect(context.joining(System.lineSeparator()))).append(System.lineSeparator()).append(System.lineSeparator());

			moduleClass.append("@").append(context.getTypeName(generatedType)).append("(value= {\"").append(InvernoCompiler.class.getCanonicalName()).append("\", \"").append(moduleInfo.getVersion()).append("\"}, date = \"").append(ZonedDateTime.now().toString()).append("\")").append(System.lineSeparator());
			moduleClass.append("public final class ").append(className).append(" extends ").append(context.getTypeName(moduleType)).append(" {").append(System.lineSeparator()).append(System.lineSeparator());

			if(module_field_modules.length() > 0) {
				moduleClass.append(module_field_modules).append(System.lineSeparator()).append(System.lineSeparator());
			}
			if(module_field_beans.length() > 0) {
				moduleClass.append(module_field_beans).append(System.lineSeparator()).append(System.lineSeparator());
			}
			
			moduleClass.append(context.indent(1)).append("private ").append(className).append("(").append(System.lineSeparator()).append(module_constructor_parameters).append(System.lineSeparator()).append(context.indent(2)).append(") {").append(System.lineSeparator());
			moduleClass.append(context.indent(2)).append("super(\"").append(moduleInfo.getQualifiedName().getValue()).append("\");").append(System.lineSeparator());
			
			if(module_constructor_modules.length() > 0) {
				moduleClass.append(System.lineSeparator()).append(module_constructor_modules).append(System.lineSeparator());
			}
			if(module_constructor_beans.length() > 0) {
				moduleClass.append(System.lineSeparator()).append(module_constructor_beans).append(System.lineSeparator());
			}
			
			moduleClass.append(context.indent(1)).append("}").append(System.lineSeparator());
			
			if(module_method_beans.length() > 0) {
				moduleClass.append(System.lineSeparator()).append(module_method_beans).append(System.lineSeparator());
			}
			
			moduleClass.append(module_builder).append(System.lineSeparator()).append(System.lineSeparator());
			moduleClass.append(module_linker);
			
			moduleClass.append(System.lineSeparator()).append("}").append(System.lineSeparator());
			
			return moduleClass;
		}
		else if(context.getMode() == GenerationMode.MODULE_BUILDER_CLASS) {
			TypeMirror moduleBuilderType = context.getTypeUtils().erasure(context.getElementUtils().getTypeElement(INVERNO_CORE_MODULE_MODULEBUILDER_CLASS).asType());
			
			StringBuilder module_builder_fields = Arrays.stream(moduleInfo.getSockets()) 
				.filter(socketInfo -> socketInfo.isWired())
				.map(socketInfo -> this.visit(socketInfo, context.withModule(moduleInfo.getQualifiedName()).withMode(GenerationMode.SOCKET_FIELD)))
				.collect(context.joining(System.lineSeparator()));
			
			StringBuilder module_builder_constructor_parameters = Arrays.stream(moduleInfo.getSockets())
				.filter(socketInfo -> !socketInfo.isOptional())
				.filter(socketInfo -> socketInfo.isWired())
				.map(socketInfo -> (socketInfo instanceof MultiSocketInfo ? new StringBuilder().append(context.getMultiTypeName(socketInfo.getType(), ((MultiSocketInfo)socketInfo).getMultiType())) : new StringBuilder().append(context.getTypeName(socketInfo.getType()))).append(" ").append(socketInfo.getQualifiedName().normalize()))
				.collect(context.joining(", "));
			
			StringBuilder module_builder_constructor_super_args = Arrays.stream(moduleInfo.getSockets())
				.filter(socketInfo -> !socketInfo.isOptional())
				.filter(socketInfo -> socketInfo.isWired())
				.map(socketInfo -> new StringBuilder().append("\"").append(socketInfo.getQualifiedName().normalize()).append("\", ").append(socketInfo.getQualifiedName().normalize()))
				.collect(context.joining(", "));
			
			StringBuilder module_builder_constructor_assignments = Arrays.stream(moduleInfo.getSockets())
				.filter(socketInfo -> !socketInfo.isOptional())
				.filter(socketInfo -> socketInfo.isWired())
				.map(socketInfo -> this.visit(socketInfo, context.withModule(moduleInfo.getQualifiedName()).withMode(GenerationMode.SOCKET_ASSIGNMENT)))
				.collect(context.joining(System.lineSeparator()));
			
			StringBuilder module_builder_build_args = Arrays.stream(moduleInfo.getSockets())
				.filter(socketInfo -> socketInfo.isWired())
				.map(socketInfo -> new StringBuilder().append("this.").append(socketInfo.getQualifiedName().normalize()))
				.collect(context.joining(", "));
			
			StringBuilder module_builder_socket_methods = Arrays.stream(moduleInfo.getSockets())
				.filter(moduleSocketInfo -> moduleSocketInfo.isOptional())
				.filter(socketInfo -> socketInfo.isWired())
				.map(socketInfo -> this.visit(socketInfo, context.withModule(moduleInfo.getQualifiedName()).withMode(GenerationMode.SOCKET_INJECTOR)))
				.collect(context.joining(System.lineSeparator() + System.lineSeparator()));

			StringBuilder moduleBuilderClass = new StringBuilder().append(context.indent(1)).append("public static final class Builder extends ").append(context.getTypeName(moduleBuilderType)).append("<").append(className).append("> {").append(System.lineSeparator()).append(System.lineSeparator());
			if(module_builder_fields.length() > 0) {
				moduleBuilderClass.append(module_builder_fields).append(System.lineSeparator()).append(System.lineSeparator());
			}
			if(module_builder_constructor_parameters.length() > 0) {
				moduleBuilderClass.append(context.indent(2)).append("public Builder(").append(module_builder_constructor_parameters).append(") {").append(System.lineSeparator());
				moduleBuilderClass.append(context.indent(3)).append("super(").append(module_builder_constructor_super_args).append(");").append(System.lineSeparator()).append(System.lineSeparator());
				moduleBuilderClass.append(module_builder_constructor_assignments).append(System.lineSeparator());
				moduleBuilderClass.append(context.indent(2)).append("}").append(System.lineSeparator()).append(System.lineSeparator());
			}
			
			moduleBuilderClass.append(context.indent(2)).append("protected ").append(className).append(" doBuild() {").append(System.lineSeparator());
			moduleBuilderClass.append(context.indent(3)).append("return new ").append(className).append("(").append(module_builder_build_args).append(");").append(System.lineSeparator());
			moduleBuilderClass.append(context.indent(2)).append("}").append(System.lineSeparator());
			
			if(module_builder_socket_methods.length() > 0) {
				moduleBuilderClass.append(System.lineSeparator()).append(module_builder_socket_methods).append(System.lineSeparator());
			}
			
			moduleBuilderClass.append(context.indent(1)).append("}");
			
			return moduleBuilderClass;
		}
		else if(context.getMode() == GenerationMode.MODULE_LINKER_CLASS) {
			TypeMirror moduleLinkerType = context.getTypeUtils().erasure(context.getElementUtils().getTypeElement(INVERNO_CORE_MODULE_LINKER_CLASS).asType());
			
			StringBuilder linker_module_args = Arrays.stream(moduleInfo.getSockets())
				.filter(socketInfo -> socketInfo.isWired())
				.map(socketInfo -> {
					StringBuilder result = new StringBuilder().append(context.indent(4));
					if(socketInfo.isOptional()) {
						result.append("(").append(context.getOptionalTypeName()).append("<").append(this.visit(socketInfo, context.withMode(GenerationMode.SOCKET_SUPPLIER))).append(">").append(")");
					}
					else {
						result.append("(").append(this.visit(socketInfo, context.withMode(GenerationMode.SOCKET_SUPPLIER))).append(")");
					}
					result.append("this.sockets.get(\"").append(socketInfo.getQualifiedName().normalize()).append("\")");

					return result ;
				})
				.collect(context.joining("," + System.lineSeparator()));

			StringBuilder linkerClass = new StringBuilder().append(context.indent(1)).append("public static final class Linker extends ").append(context.getTypeName(moduleLinkerType)).append("<").append(className).append("> {").append(System.lineSeparator()).append(System.lineSeparator());
			
			linkerClass.append(context.indent(2)).append("public Linker(").append(context.getMapTypeName()).append("<String, Object> sockets) {").append(System.lineSeparator());
			linkerClass.append(context.indent(3)).append("super(sockets);").append(System.lineSeparator());
			linkerClass.append(context.indent(2)).append("}").append(System.lineSeparator()).append(System.lineSeparator());

			
			linkerClass.append(context.indent(2)).append("@SuppressWarnings(\"unchecked\")").append(System.lineSeparator());
			linkerClass.append(context.indent(2)).append("protected ").append(className).append(" link() {").append(System.lineSeparator());
			linkerClass.append(context.indent(3)).append("return new ").append(className).append("(").append(System.lineSeparator());
			linkerClass.append(linker_module_args).append(System.lineSeparator());
			linkerClass.append(context.indent(3)).append(");").append(System.lineSeparator());
			linkerClass.append(context.indent(2)).append("}").append(System.lineSeparator());
			
			linkerClass.append(context.indent(1)).append("}");
			
			return linkerClass;
		}
		else if(context.getMode() == GenerationMode.COMPONENT_MODULE_NEW) {
			TypeMirror componentModuleType = context.getElementUtils().getTypeElement(moduleInfo.getQualifiedName().getClassName()).asType();

			StringBuilder component_module_arguments = Arrays.stream(moduleInfo.getSockets())
				.filter(socketInfo -> socketInfo.isWired())
				.map(socketInfo -> {
					StringBuilder ret = new StringBuilder().append(context.indent(3)).append(context.getMapTypeName()).append(".entry(\"").append(socketInfo.getQualifiedName().normalize()).append("\", ");
					ret.append(this.visit(socketInfo, context.withMode(GenerationMode.COMPONENT_MODULE_BEAN_REFERENCE).withIndentDepth(4)));
					ret.append(")");
					return ret;
				})
				.collect(context.joining("," + System.lineSeparator()));
			
			StringBuilder moduleNew = new StringBuilder().append(context.indent(2)).append("this.").append(context.getFieldName(moduleInfo.getQualifiedName())).append(" = this.with(new ").append(context.getTypeName(componentModuleType)).append(".Linker(");
			if(component_module_arguments.length() > 0) {
				moduleNew.append(context.getMapTypeName()).append(".ofEntries(").append(System.lineSeparator());
				moduleNew.append(component_module_arguments);
				moduleNew.append(System.lineSeparator()).append(context.indent(2)).append(")");
			}
			else {
				moduleNew.append(context.getMapTypeName()).append(".of()");
			}
			moduleNew.append("));");
			
			return moduleNew;
		}
		else if(context.getMode() == GenerationMode.COMPONENT_MODULE_FIELD) {
			TypeMirror componentModuleType = context.getElementUtils().getTypeElement(moduleInfo.getQualifiedName().getClassName()).asType();
			return new StringBuilder().append(context.indent(1)).append("private ").append(context.getTypeName(componentModuleType)).append(" ").append(context.getFieldName(moduleInfo.getQualifiedName())).append(";");
		}
		return null;
	}

	@Override
	public StringBuilder visit(BeanInfo beanInfo, ModuleClassGenerationContext context) {
		if(beanInfo instanceof NestedBeanInfo) {
			return this.visit((NestedBeanInfo)beanInfo, context);
		}
		else if(beanInfo instanceof ModuleBeanInfo) {
			return this.visit((ModuleBeanInfo)beanInfo, context);
		}
		else if(beanInfo instanceof SocketBeanInfo) {
			return this.visit((SocketBeanInfo)beanInfo, context);
		}
		return new StringBuilder();
	}
	
	@Override
	public StringBuilder visit(NestedBeanInfo nestedBeanInfo, ModuleClassGenerationContext context) {
		if(context.getMode() == GenerationMode.BEAN_REFERENCE) {
			return new StringBuilder().append(context.getOptionalTypeName()).append(".ofNullable(").append(this.visit(nestedBeanInfo.getProvidingBean(), context)).append(".").append(nestedBeanInfo.getName()).append("()).orElseThrow(() -> new ").append(context.getNpeTypeName()).append("(\"").append(nestedBeanInfo.getQualifiedName().getSimpleValue()).append("\"))");
		}
		return new StringBuilder();
	}

	@Override
	public StringBuilder visit(ModuleBeanInfo moduleBeanInfo, ModuleClassGenerationContext context) {
		if(context.getMode() == GenerationMode.BEAN_FIELD) {
			TypeMirror beanType = moduleBeanInfo instanceof OverridableBeanInfo && moduleBeanInfo.getProvidedType() != null ? moduleBeanInfo.getProvidedType() : moduleBeanInfo.getType();
			if(moduleBeanInfo instanceof MutatorBeanInfo && ((MutatorBeanInfo)moduleBeanInfo).getMutatingSocket().isOptional()) {
				beanType = context.getTypeUtils().getDeclaredType(context.getElementUtils().getTypeElement(Optional.class.getCanonicalName()), beanType);
			}
			TypeMirror moduleBeanType = context.getTypeUtils().getDeclaredType(context.getElementUtils().getTypeElement(INVERNO_CORE_MODULE_BEAN_CLASS), beanType);
			return new StringBuilder().append(context.indent(1)).append("private ").append(context.getTypeName(moduleBeanType)).append(" ").append(context.getFieldName(moduleBeanInfo.getQualifiedName())).append(";");
		}
		else if(context.getMode() == GenerationMode.BEAN_ACCESSOR) {
			StringBuilder beanAccessor = new StringBuilder();
			TypeMirror beanType = moduleBeanInfo.getProvidedType() != null ? moduleBeanInfo.getProvidedType() : moduleBeanInfo.getType();
			if(moduleBeanInfo instanceof MutatorBeanInfo && ((MutatorBeanInfo)moduleBeanInfo).getMutatingSocket().isOptional()) {
				beanType = context.getTypeUtils().getDeclaredType(context.getElementUtils().getTypeElement(Optional.class.getCanonicalName()), beanType);
			}
			beanAccessor.append(context.indent(1)).append(moduleBeanInfo.getVisibility().equals(Bean.Visibility.PUBLIC) ? "public " : "private ").append(context.getTypeName(beanType)).append(" ").append(moduleBeanInfo.getQualifiedName().normalize()).append("() {").append(System.lineSeparator());
			beanAccessor.append(context.indent(2)).append("return this.").append(context.getFieldName(moduleBeanInfo.getQualifiedName())).append(".get()").append(";").append(System.lineSeparator());
			beanAccessor.append(context.indent(1)).append("}").append(System.lineSeparator());
			
			return beanAccessor;
		}
		else if(context.getMode() == GenerationMode.BEAN_NEW) {
			if(moduleBeanInfo instanceof OverridableBeanInfo) {
				StringBuilder beanNew = this.visit(((OverridableBeanInfo)moduleBeanInfo).getOverridableBean(), context);
				beanNew = beanNew.delete(beanNew.length() - 2, beanNew.length());
				
				beanNew.append(context.indent(1)).append(".override(").append(((OverridableBeanInfo)moduleBeanInfo).getOverridingSocket().getQualifiedName().normalize()).append("_override)").append(System.lineSeparator());
				beanNew.append(context.indent(2)).append(");");
				
				return beanNew;
			}
			else {
				StringBuilder variable = new StringBuilder(context.getFieldName(moduleBeanInfo.getQualifiedName()));
				
				TypeMirror beanType;
				TypeMirror beanBuilderType;
				if(moduleBeanInfo instanceof WrapperBeanInfo) {
					beanType = ((WrapperBeanInfo)moduleBeanInfo).getWrapperType();
					beanBuilderType = context.getTypeUtils().erasure(context.getElementUtils().getTypeElement(INVERNO_CORE_MODULE_WRAPPERBEANBUILDER_CLASS).asType());
				}
				else if(moduleBeanInfo instanceof MutatorBeanInfo) {
					variable.append("_mutator");
					beanType = ((MutatorBeanInfo)moduleBeanInfo).getMutatorType();
					beanBuilderType = context.getTypeUtils().erasure(context.getElementUtils().getTypeElement(INVERNO_CORE_MODULE_MODULEBEANBUILDER_CLASS).asType());
				}
				else {
					beanType = moduleBeanInfo.getType();
					beanBuilderType = context.getTypeUtils().erasure(context.getElementUtils().getTypeElement(INVERNO_CORE_MODULE_MODULEBEANBUILDER_CLASS).asType());
				}
				
				StringBuilder beanNew = new StringBuilder().append(context.indent(2)).append("this.").append(context.getFieldName(moduleBeanInfo.getQualifiedName())).append(" = this.with(").append(context.getTypeName(beanBuilderType)).append(System.lineSeparator());
				
				switch (moduleBeanInfo.getStrategy()) {
					case SINGLETON:
						beanNew.append(context.indent(3)).append(".singleton(\"").append(moduleBeanInfo.getQualifiedName().getSimpleValue()).append("\", () -> {").append(System.lineSeparator());
						break;
					case PROTOTYPE:
						beanNew.append(context.indent(3)).append(".prototype(\"").append(moduleBeanInfo.getQualifiedName().getSimpleValue()).append("\", () -> {").append(System.lineSeparator());
						break;
					default:
					throw new IllegalArgumentException("Unkown bean strategy: " + moduleBeanInfo.getStrategy());
				}
				
				beanNew.append(context.indent(4)).append(context.getTypeName(beanType)).append(" ").append(variable).append(" = new ").append(context.getTypeName(beanType)).append("(");
				if(moduleBeanInfo.getRequiredSockets().length > 0) {
					beanNew.append(System.lineSeparator());
					beanNew.append(Arrays.stream(moduleBeanInfo.getRequiredSockets())
						.sorted((ModuleBeanSocketInfo s1, ModuleBeanSocketInfo s2) -> {
							if(s1.getSocketElement().get() != s2.getSocketElement().get()) {
								throw new IllegalStateException("Comparing required sockets with different socket elements");
							}
							List<String> orderedDependencyNames = s1.getSocketElement().get().getParameters().stream().map(element -> element.getSimpleName().toString()).collect(Collectors.toList());
							return orderedDependencyNames.indexOf(s1.getQualifiedName().getSimpleValue()) - orderedDependencyNames.indexOf(s2.getQualifiedName().getSimpleValue());
						})
						.map(socketInfo -> new StringBuilder().append(context.indent(5)).append(this.visit(socketInfo, context.withMode(GenerationMode.BEAN_REFERENCE).withIndentDepth(5))))
						.collect(context.joining("," + System.lineSeparator())));
					beanNew.append(System.lineSeparator()).append(context.indent(4)).append(");").append(System.lineSeparator());
				}
				else {
					beanNew.append(");").append(System.lineSeparator());
				}
				beanNew.append(Arrays.stream(moduleBeanInfo.getOptionalSockets())
					.filter(socketInfo -> socketInfo.isResolved())
					.map(socketInfo -> {
						StringBuilder optSocket = new StringBuilder().append(context.indent(4));
						if(socketInfo.isLazy()) {
							optSocket.append(variable).append(".").append(socketInfo.getSocketElement().get().getSimpleName().toString()).append("(").append(this.visit(socketInfo, context.withMode(GenerationMode.BEAN_REFERENCE).withIndentDepth(4))).append(");");
						}
						else {
							optSocket.append(this.visit(socketInfo, context.withMode(GenerationMode.BEAN_OPTIONAL_REFERENCE).withIndentDepth(4))).append(".ifPresent(").append(variable).append("::").append(socketInfo.getSocketElement().get().getSimpleName().toString()).append(");");
						}
						return optSocket;
					})
					.collect(context.joining(System.lineSeparator()))).append(System.lineSeparator());
	
				beanNew.append(context.indent(4)).append("return ");
				if(moduleBeanInfo instanceof MutatorBeanInfo) {
					if(((MutatorBeanInfo) moduleBeanInfo).getMutatingSocket().isOptional()) {
						beanNew.append(this.visit(((MutatorBeanInfo) moduleBeanInfo).getMutatingSocket(), context.withMode(GenerationMode.BEAN_OPTIONAL_REFERENCE))).append(".map(").append(variable).append("::apply)");
					}
					else {
						beanNew.append(variable).append(".apply(").append(this.visit(((MutatorBeanInfo) moduleBeanInfo).getMutatingSocket(), context.withMode(GenerationMode.BEAN_REFERENCE))).append(")");
					}
				}
				else {
					beanNew.append(variable);
				}
				beanNew.append(";").append(System.lineSeparator());
				beanNew.append(context.indent(3)).append("})").append(System.lineSeparator());
	
				if(moduleBeanInfo.getInitElements().length > 0) {
					beanNew.append(Arrays.stream(moduleBeanInfo.getInitElements())
						.map(element -> new StringBuilder().append(context.indent(3)).append(".init(").append(context.getTypeName(beanType)).append("::").append(element.getSimpleName().toString()).append(")"))
						.collect(context.joining(System.lineSeparator()))).append(System.lineSeparator());
				}
					
				if(moduleBeanInfo.getDestroyElements().length > 0) {
					beanNew.append(Arrays.stream(moduleBeanInfo.getDestroyElements())
						.map(element -> new StringBuilder().append(context.indent(3)).append(".destroy(").append(context.getTypeName(beanType)).append("::").append(element.getSimpleName().toString()).append(")"))
						.collect(context.joining(System.lineSeparator()))).append(System.lineSeparator());
				}
				
				beanNew.append(context.indent(2)).append(");");
				
				return beanNew;
			}
		}
		else if(context.getMode() == GenerationMode.BEAN_REFERENCE) {
			if(moduleBeanInfo.getQualifiedName().getModuleQName().equals(context.getModule())) {
				// We can't use bean accessor for internal beans since provided types are ignored inside a module
				return new StringBuilder().append("this.").append(context.getFieldName(moduleBeanInfo.getQualifiedName())).append(".get()");
			}
			else {
				return new StringBuilder().append("this.").append(context.getFieldName(moduleBeanInfo.getQualifiedName().getModuleQName())).append(".").append(moduleBeanInfo.getQualifiedName().normalize()).append("()");
			}
		}
		else if(context.getMode() == GenerationMode.BEAN_OPTIONAL_REFERENCE) {
			if(moduleBeanInfo instanceof MutatorBeanInfo && ((MutatorBeanInfo)moduleBeanInfo).getMutatingSocket().isOptional()) {
				return this.visit(moduleBeanInfo, context.withMode(GenerationMode.BEAN_REFERENCE));
			}
			else {
				return new StringBuilder(context.getOptionalTypeName()).append(".ofNullable(").append(this.visit(moduleBeanInfo, context.withMode(GenerationMode.BEAN_REFERENCE))).append(")");
			}
		}
		return new StringBuilder();
	}

	@Override
	public StringBuilder visit(WrapperBeanInfo moduleWrapperBeanInfo, ModuleClassGenerationContext context) {
		return this.visit((ModuleBeanInfo)moduleWrapperBeanInfo, context);
	}
	
	@Override
	public StringBuilder visit(OverridableBeanInfo overridableBeanInfo, ModuleClassGenerationContext context) {
		return this.visit((ModuleBeanInfo)overridableBeanInfo, context);
	}
	
	@Override
	public StringBuilder visit(OverridingSocketBeanInfo overridingSocketBeanInfo, ModuleClassGenerationContext context) {
		return this.visit((SingleSocketBeanInfo)overridingSocketBeanInfo, context);
	}

	@Override
	public StringBuilder visit(MutatorBeanInfo mutatorBeanInfo, ModuleClassGenerationContext context) {
		return this.visit((ModuleBeanInfo)mutatorBeanInfo, context);
	}

	@Override
	public StringBuilder visit(SocketInfo socketInfo, ModuleClassGenerationContext context) {
		if(socketInfo instanceof SingleSocketInfo) {
			return this.visit((SingleSocketInfo)socketInfo, context);
		}
		else if(socketInfo instanceof MultiSocketInfo) {
			return this.visit((MultiSocketInfo)socketInfo, context);
		}
		return new StringBuilder();
	}
	
	@Override
	public StringBuilder visit(SingleSocketInfo singleSocketInfo, ModuleClassGenerationContext context) {
		if(!singleSocketInfo.isResolved()) {
			return new StringBuilder().append("null");
		}
		if(context.getMode() == GenerationMode.BEAN_REFERENCE || context.getMode() == GenerationMode.BEAN_OPTIONAL_REFERENCE) {
			return this.visit(singleSocketInfo.getBean(), context);
		}
		return new StringBuilder();
	}

	@Override
	public StringBuilder visit(MultiSocketInfo multiSocketInfo, ModuleClassGenerationContext context) {
		if(context.getMode() == GenerationMode.BEAN_REFERENCE || context.getMode() == GenerationMode.BEAN_OPTIONAL_REFERENCE) {
			final TypeMirror unwildDependencyType;
			if(multiSocketInfo.getType().getKind().equals(TypeKind.WILDCARD)) {
				if(((WildcardType)multiSocketInfo.getType()).getExtendsBound() != null) {
					unwildDependencyType = ((WildcardType)multiSocketInfo.getType()).getExtendsBound();
				}
				else if(((WildcardType)multiSocketInfo.getType()).getSuperBound() != null) {
					// TODO if we want to be strict here we should resolve the upper most type based on the beans injected in the socket
					// but we can simply use Object here, it's not ideal but it works
					unwildDependencyType = context.getElementUtils().getTypeElement(Object.class.getCanonicalName()).asType();
				}
				else {
					unwildDependencyType = context.getElementUtils().getTypeElement(Object.class.getCanonicalName()).asType();
				}
			}
			else {
				unwildDependencyType = multiSocketInfo.getType();
			}
			
			if(multiSocketInfo.isResolved()) {
				TypeMirror beanAggregatorType = context.getTypeUtils().erasure(context.getElementUtils().getTypeElement(INVERNO_CORE_MODULE_BEANAGGREGATOR_CLASS).asType());

				boolean lazy = multiSocketInfo instanceof ModuleBeanMultiSocketInfo && ((ModuleBeanMultiSocketInfo)multiSocketInfo).isLazy();
				
				StringBuilder beanSocketReference = new StringBuilder().append("new ").append(context.getTypeName(beanAggregatorType)).append("<");
				if(lazy) {
					beanSocketReference.append(context.getSupplierTypeName()).append("<").append(context.getTypeName(unwildDependencyType)).append(">");
				}
				else {
					beanSocketReference.append(context.getTypeName(unwildDependencyType));
				}
				beanSocketReference.append(">()").append(System.lineSeparator());
				beanSocketReference.append(Arrays.stream(multiSocketInfo.getBeans())
					.map(beanInfo -> {
						StringBuilder beanRef = new StringBuilder(context.indent(1)).append(".add(");
						if(lazy) {
							beanRef.append("() -> ");
						}
						return beanRef.append(this.visit(beanInfo, context.withMode(GenerationMode.BEAN_REFERENCE))).append(")");
					})
					.collect(context.joining(System.lineSeparator()))).append(System.lineSeparator());

				switch (multiSocketInfo.getMultiType()) {
					case ARRAY:
						beanSocketReference.append(context.indent(0));
						if(context.getMode() == GenerationMode.BEAN_REFERENCE) {
							beanSocketReference.append(".toArray(");
						}
						else {
							beanSocketReference.append(".toOptionalArray(");
						}	beanSocketReference.append(context.getTypeName(unwildDependencyType)).append("[]::new)");
						break;
					case COLLECTION:
					case LIST:
						beanSocketReference.append(context.indent(0));
						if(context.getMode() == GenerationMode.BEAN_REFERENCE) {
							beanSocketReference.append(".toList()");
						}
						else {
							beanSocketReference.append(".toOptionalList()");
						}	break;
					case SET:
						beanSocketReference.append(context.indent(0));
						if(context.getMode() == GenerationMode.BEAN_REFERENCE) {
							beanSocketReference.append(".toSet()");
						}
						else {
							beanSocketReference.append(".toOptionalSet()");
						}	break;
					default:
						break;
				}

				return beanSocketReference;
			}
			else {
				if(context.getMode() == GenerationMode.BEAN_REFERENCE) {
					switch (multiSocketInfo.getMultiType()) {
						case ARRAY:
							return new StringBuilder().append("new ").append(context.getTypeName(unwildDependencyType)).append("[0]");
						case COLLECTION:
						case LIST:
							return new StringBuilder().append(context.getListTypeName()).append(".of()");
						case SET:
							return new StringBuilder().append(context.getSetTypeName()).append(".of()");
						default:
							break;
					}
				}
				else {
					return new StringBuilder().append(context.getOptionalTypeName()).append(".empty()");
				}
			}
		}
		return new StringBuilder();
	}
	
	@Override
	public StringBuilder visit(ModuleBeanSocketInfo beanSocketInfo, ModuleClassGenerationContext context) {
		if(beanSocketInfo instanceof ModuleBeanSingleSocketInfo) {
			return this.visit((ModuleBeanSingleSocketInfo)beanSocketInfo, context);
		}
		else if(beanSocketInfo instanceof ModuleBeanMultiSocketInfo) {
			return this.visit((ModuleBeanMultiSocketInfo)beanSocketInfo, context);
		}
		return new StringBuilder();
	}

	@Override
	public StringBuilder visit(ModuleBeanSingleSocketInfo beanSingleSocketInfo, ModuleClassGenerationContext context) {
		if(beanSingleSocketInfo.isLazy() && (context.getMode() == GenerationMode.BEAN_REFERENCE || context.getMode() == GenerationMode.BEAN_OPTIONAL_REFERENCE)) {
			return new StringBuilder("() -> ").append(this.visit((SingleSocketInfo)beanSingleSocketInfo, context));
		}
		return this.visit((SingleSocketInfo)beanSingleSocketInfo, context);
	}

	@Override
	public StringBuilder visit(ModuleBeanMultiSocketInfo beanMultiSocketInfo, ModuleClassGenerationContext context) {
		return this.visit((MultiSocketInfo)beanMultiSocketInfo, context);
	}

	@Override
	public StringBuilder visit(SocketBeanInfo socketBeanInfo, ModuleClassGenerationContext context) {
		if(context.getMode() == GenerationMode.SOCKET_PARAMETER) {
			StringBuilder socketParameter = new StringBuilder();
			if(socketBeanInfo.isWired()) {
				TypeMirror socketAnnotationType = context.getElementUtils().getTypeElement(INVERNO_CORE_MODULE_SOCKET_ANNOTATION).asType();
				
				socketParameter.append("@").append(context.getTypeName(socketAnnotationType)).append("(name = \"").append(socketBeanInfo.getQualifiedName().normalize()).append("\"");
				if(socketBeanInfo.getWiredBeans().length > 0) {
					socketParameter.append(", wiredTo = {").append(Arrays.stream(socketBeanInfo.getWiredBeans()).map(beanQName -> new StringBuilder().append("\"").append(beanQName.getSimpleValue()).append("\"")).collect(context.joining(", "))).append("}");
				}
				socketParameter.append(") ");
			}
			
			if(socketBeanInfo.getSelectors().length > 0) {
				// TODO use a recursive method to add imports and reduce the generated line
				socketParameter.append(Arrays.stream(socketBeanInfo.getSelectors()).map(selector -> selector.toString()).collect(context.joining(", "))).append(" ");
			}
			
			if(socketBeanInfo.isOptional()) {
				socketParameter.append(context.getOptionalTypeName()).append("<").append(this.visit(socketBeanInfo, context.withMode(GenerationMode.SOCKET_SUPPLIER))).append(">");
			}
			else {
				socketParameter.append(this.visit(socketBeanInfo, context.withMode(GenerationMode.SOCKET_SUPPLIER)));
			}
			
			socketParameter.append(" ").append(socketBeanInfo.getQualifiedName().normalize());
			if(socketBeanInfo instanceof OverridingSocketBeanInfo) {
				socketParameter.append("_override");
			}
			
			return socketParameter;
		}
		else if(context.getMode() == GenerationMode.SOCKET_FIELD) {
			if(socketBeanInfo.isOptional()) {
				return new StringBuilder().append(context.indent(2)).append("private ").append(context.getOptionalTypeName()).append("<").append(this.visit(socketBeanInfo, context.withMode(GenerationMode.SOCKET_SUPPLIER))).append("> ").append(socketBeanInfo.getQualifiedName().normalize()).append(" = ").append(context.getOptionalTypeName()).append(".empty();");
			}
			else {
				return new StringBuilder().append(context.indent(2)).append("private ").append(this.visit(socketBeanInfo, context.withMode(GenerationMode.SOCKET_SUPPLIER))).append(" ").append(socketBeanInfo.getQualifiedName().normalize()).append(";");
			}
		}
		else if(context.getMode() == GenerationMode.SOCKET_ASSIGNMENT) {
			return new StringBuilder().append(context.indent(3)).append("this.").append(socketBeanInfo.getQualifiedName().normalize()).append(" = () -> ").append(socketBeanInfo.getQualifiedName().normalize()).append(";");
		}
		else if(context.getMode() == GenerationMode.SOCKET_INJECTOR) {
			StringBuilder plugName = new StringBuilder().append(socketBeanInfo.getQualifiedName().normalize());
			
			StringBuilder result = new StringBuilder().append(context.indent(2)).append("public Builder set").append(Character.toUpperCase(plugName.charAt(0))).append(plugName.substring(1)).append("(").append((socketBeanInfo instanceof MultiSocketInfo ? context.getMultiTypeName(socketBeanInfo.getType(), ((MultiSocketInfo)socketBeanInfo).getMultiType() ) : context.getTypeName(socketBeanInfo.getType()))).append(" ").append(plugName).append(") {").append(System.lineSeparator());
			if(socketBeanInfo.isOptional()) {
				result.append(context.indent(3)).append("this.").append(plugName).append(" = ").append(context.getOptionalTypeName()).append(".ofNullable(").append(plugName).append(" != null ? () -> ").append(plugName).append(" : null);").append(System.lineSeparator());
			}
			else {
				result.append(context.indent(3)).append("this.").append(plugName).append(" = () -> ").append(plugName).append(";").append(System.lineSeparator());
			}
			result.append(context.indent(3)).append("return this;").append(System.lineSeparator());
			result.append(context.indent(2)).append("}");
			
			return result;
		}
		else if(context.getMode() == GenerationMode.SOCKET_SUPPLIER) {
			if(socketBeanInfo instanceof SingleSocketBeanInfo) {
				return this.visit((SingleSocketBeanInfo)socketBeanInfo, context);
			}
			else if(socketBeanInfo instanceof MultiSocketBeanInfo) {
				return this.visit((MultiSocketBeanInfo)socketBeanInfo, context);
			}
		}
		else if(context.getMode() == GenerationMode.BEAN_REFERENCE) {
			if(socketBeanInfo.isOptional()) {
				return new StringBuilder().append(socketBeanInfo.getQualifiedName().normalize()).append(".orElse(() -> null).get()");
			}
			else {
				return new StringBuilder().append(socketBeanInfo.getQualifiedName().normalize()).append(".get()");
			}
		}
		else if(context.getMode() == GenerationMode.BEAN_OPTIONAL_REFERENCE) {
			if(socketBeanInfo.isOptional()) {
				return new StringBuilder().append(socketBeanInfo.getQualifiedName().normalize()).append(".map(").append(context.getSupplierTypeName()).append("::get)");
			}
			else {
				return new StringBuilder().append(context.getOptionalTypeName()).append(".ofNullable(").append(socketBeanInfo.getQualifiedName().normalize()).append(".get())");
			}
		}
		else if(context.getMode() == GenerationMode.COMPONENT_MODULE_BEAN_REFERENCE) {
			if(!socketBeanInfo.isOptional() || socketBeanInfo.isResolved()) {
				StringBuilder result = new StringBuilder();
				if(socketBeanInfo instanceof SingleSocketInfo) {
					result.append(this.visit((SingleSocketBeanInfo)socketBeanInfo, context));
				}
				else if(socketBeanInfo instanceof MultiSocketInfo) {
					result.append(this.visit((MultiSocketBeanInfo)socketBeanInfo, context));
				}
				
				if(socketBeanInfo.isOptional()) {
					return new StringBuilder().append(context.getOptionalTypeName()).append(".ofNullable(").append(result).append(")");
				}
				return result;
			}
			else {
				return new StringBuilder().append(context.getOptionalTypeName()).append(".empty()");
			}
		}
		return new StringBuilder();
	}

	@Override
	public StringBuilder visit(SingleSocketBeanInfo singleSocketBeanInfo, ModuleClassGenerationContext context) {
		if(context.getMode() == GenerationMode.COMPONENT_MODULE_BEAN_REFERENCE) {
			StringBuilder result = new StringBuilder().append("(").append(this.visit(singleSocketBeanInfo, context.withMode(GenerationMode.SOCKET_SUPPLIER))).append(")");
			if(singleSocketBeanInfo.isResolved()) {
				result.append("() -> ").append(this.visit((SingleSocketInfo)singleSocketBeanInfo, context.withMode(GenerationMode.BEAN_REFERENCE)));
			}
			else {
				result.append("null");
			}
			return result;
		}
		else if(context.getMode() == GenerationMode.SOCKET_SUPPLIER) {
			StringBuilder result = new StringBuilder();
			result.append(context.getSupplierTypeName()).append("<").append(context.getTypeName(singleSocketBeanInfo.getType())).append(">");
			return result;
		}
		return new StringBuilder();
	}

	@Override
	public StringBuilder visit(MultiSocketBeanInfo multiSocketBeanInfo, ModuleClassGenerationContext context) {
		if(context.getMode() == GenerationMode.COMPONENT_MODULE_BEAN_REFERENCE) {
			StringBuilder result = new StringBuilder().append("(").append(this.visit(multiSocketBeanInfo, context.withMode(GenerationMode.SOCKET_SUPPLIER))).append(")");			
			if(multiSocketBeanInfo.isResolved()) {
				result.append("() -> ").append(this.visit((MultiSocketInfo)multiSocketBeanInfo, context.withMode(GenerationMode.BEAN_REFERENCE)));
			}
			else {
				result.append("null");
			}
			return result;
		}
		else if(context.getMode() == GenerationMode.SOCKET_SUPPLIER) {
			StringBuilder result = new StringBuilder();
			result.append(context.getSupplierTypeName()).append("<");
			switch (multiSocketBeanInfo.getMultiType()) {
				case ARRAY:
					result.append(context.getTypeName(multiSocketBeanInfo.getType())).append("[]");
					break;
				case COLLECTION:
					result.append(context.getCollectionTypeName()).append("<").append(context.getTypeName(multiSocketBeanInfo.getType())).append(">");
					break;
				case LIST:
					result.append(context.getListTypeName()).append("<").append(context.getTypeName(multiSocketBeanInfo.getType())).append(">");
					break;
				case SET:
					result.append(context.getSetTypeName()).append("<").append(context.getTypeName(multiSocketBeanInfo.getType())).append(">");
					break;
				default:
					break;
			}
			result.append(">");
			return result;
		}
		return new StringBuilder();
	}
}
