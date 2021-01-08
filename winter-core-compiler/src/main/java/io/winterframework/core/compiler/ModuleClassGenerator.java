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
package io.winterframework.core.compiler;

import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.type.WildcardType;

import io.winterframework.core.annotation.Bean;
import io.winterframework.core.compiler.ModuleClassGenerationContext.GenerationMode;
import io.winterframework.core.compiler.spi.BeanInfo;
import io.winterframework.core.compiler.spi.ModuleBeanInfo;
import io.winterframework.core.compiler.spi.ModuleBeanMultiSocketInfo;
import io.winterframework.core.compiler.spi.ModuleBeanSingleSocketInfo;
import io.winterframework.core.compiler.spi.ModuleBeanSocketInfo;
import io.winterframework.core.compiler.spi.ModuleInfo;
import io.winterframework.core.compiler.spi.ModuleInfoVisitor;
import io.winterframework.core.compiler.spi.MultiSocketBeanInfo;
import io.winterframework.core.compiler.spi.MultiSocketInfo;
import io.winterframework.core.compiler.spi.MultiSocketType;
import io.winterframework.core.compiler.spi.NestedBeanInfo;
import io.winterframework.core.compiler.spi.OverridableBeanInfo;
import io.winterframework.core.compiler.spi.OverridingSocketBeanInfo;
import io.winterframework.core.compiler.spi.SingleSocketBeanInfo;
import io.winterframework.core.compiler.spi.SingleSocketInfo;
import io.winterframework.core.compiler.spi.SocketBeanInfo;
import io.winterframework.core.compiler.spi.SocketInfo;
import io.winterframework.core.compiler.spi.WrapperBeanInfo;

/**
 * <p>A {@link ModuleInfoVisitor} implementation that generates a Winter module class.</p>
 * 
 * @author jkuhn
 *
 */
class ModuleClassGenerator implements ModuleInfoVisitor<StringBuilder, ModuleClassGenerationContext> {

	private static final String WINTER_CORE_PACKAGE = "io.winterframework.core.v1";
	
	private static final String WINTER_CORE_MODULE_CLASS = WINTER_CORE_PACKAGE + ".Module";
	private static final String WINTER_CORE_MODULE_MODULEBUILDER_CLASS = WINTER_CORE_PACKAGE + ".Module.ModuleBuilder";
	private static final String WINTER_CORE_MODULE_LINKER_CLASS = WINTER_CORE_PACKAGE + ".Module.ModuleLinker";
	private static final String WINTER_CORE_MODULE_BEAN_CLASS = WINTER_CORE_PACKAGE + ".Module.Bean";
	private static final String WINTER_CORE_MODULE_WRAPPERBEANBUILDER_CLASS = WINTER_CORE_PACKAGE + ".Module.WrapperBeanBuilder";
	private static final String WINTER_CORE_MODULE_MODULEBEANBUILDER_CLASS = WINTER_CORE_PACKAGE + ".Module.ModuleBeanBuilder";
	private static final String WINTER_CORE_MODULE_BEANAGGREGATOR_CLASS = WINTER_CORE_PACKAGE + ".Module.BeanAggregator";
	private static final String WINTER_CORE_MODULE_SOCKET_ANNOTATION = WINTER_CORE_PACKAGE + ".Module.Socket";
	
	@Override
	public StringBuilder visit(ModuleInfo moduleInfo, ModuleClassGenerationContext context) {
		String className = moduleInfo.getQualifiedName().getClassName();
		String packageName = className.lastIndexOf(".") != -1 ? className.substring(0, className.lastIndexOf(".")) : "";
		className = className.substring(packageName.length() + 1);
		
		if(context.getMode() == GenerationMode.MODULE_CLASS) {
			TypeMirror generatedType = context.getElementUtils().getTypeElement(context.getElementUtils().getModuleElement("java.compiler"), "javax.annotation.processing.Generated").asType();
			TypeMirror moduleType = context.getElementUtils().getTypeElement(WINTER_CORE_MODULE_CLASS).asType();

			context.addImport(className, moduleInfo.getQualifiedName().getClassName());
			context.addImport("Builder", moduleInfo.getQualifiedName().getClassName() + ".Builder");
			
			// Fields
			StringBuilder module_field_beans = Arrays.stream(moduleInfo.getBeans())
				.map(moduleBeanInfo -> this.visit(moduleBeanInfo, context.withModule(moduleInfo.getQualifiedName()).withMode(GenerationMode.BEAN_FIELD)))
				.collect(context.joining("\n"));
			StringBuilder module_field_modules = Arrays.stream(moduleInfo.getModules())
				.map(componentModuleInfo -> this.visit(componentModuleInfo, context.withModule(moduleInfo.getQualifiedName()).withMode(GenerationMode.COMPONENT_MODULE_FIELD)))
				.collect(context.joining("\n"));
			
			StringBuilder module_constructor_parameters = Arrays.stream(moduleInfo.getSockets()) 
				.filter(socketInfo -> socketInfo.isWired())
				.map(socketInfo -> this.visit(socketInfo , context.withModule(moduleInfo.getQualifiedName()).withMode(GenerationMode.SOCKET_PARAMETER)))
				.collect(context.joining(", "));
			
			StringBuilder module_constructor_modules = Arrays.stream(moduleInfo.getModules())
				.map(componentModuleInfo -> this.visit(componentModuleInfo, context.withModule(moduleInfo.getQualifiedName()).withMode(GenerationMode.COMPONENT_MODULE_NEW)))
				.collect(context.joining("\n"));
			
			StringBuilder module_constructor_beans = Arrays.stream(moduleInfo.getBeans())
				.map(moduleBeanInfo -> this.visit(moduleBeanInfo, context.withModule(moduleInfo.getQualifiedName()).withMode(GenerationMode.BEAN_NEW)))
				.collect(context.joining("\n"));
			
			StringBuilder module_method_beans = Arrays.stream(moduleInfo.getBeans())
				.map(moduleBeanInfo -> this.visit(moduleBeanInfo, context.withModule(moduleInfo.getQualifiedName()).withMode(GenerationMode.BEAN_ACCESSOR)))
				.collect(context.joining("\n"));
			
			StringBuilder module_builder = this.visit(moduleInfo, context.withModule(moduleInfo.getQualifiedName()).withMode(GenerationMode.MODULE_BUILDER_CLASS));
			StringBuilder module_linker = this.visit(moduleInfo, context.withModule(moduleInfo.getQualifiedName()).withMode(GenerationMode.MODULE_LINKER_CLASS));
			
			StringBuilder moduleClass = new StringBuilder();

			if(!packageName.equals("")) {
				moduleClass.append("package ").append(packageName).append(";").append("\n\n");
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
			
			moduleClass.append(context.getImports().stream().sorted().filter(i -> i.lastIndexOf(".") > 0 && !i.substring(0, i.lastIndexOf(".")).equals(packageName)).map(i -> new StringBuilder().append("import ").append(i).append(";")).collect(context.joining("\n"))).append("\n\n");

			moduleClass.append("@").append(context.getTypeName(generatedType)).append("(value= {\"").append(WinterCompiler.class.getCanonicalName()).append("\", \"").append(moduleInfo.getVersion()).append("\"}, date = \"").append(ZonedDateTime.now().toString() +"\")\n");
			moduleClass.append("public class ").append(className).append(" extends ").append(context.getTypeName(moduleType)).append(" {").append("\n\n");

			if(module_field_modules.length() > 0) {
				moduleClass.append(module_field_modules).append("\n\n");
			}
			if(module_field_beans.length() > 0) {
				moduleClass.append(module_field_beans).append("\n\n");
			}
			
			moduleClass.append(context.indent(1)).append("private ").append(className).append("(").append(module_constructor_parameters).append(") {\n");
			moduleClass.append(context.indent(2)).append("super(\"").append(moduleInfo.getQualifiedName().getValue()).append("\");\n");
			
			if(module_constructor_modules.length() > 0) {
				moduleClass.append("\n").append(module_constructor_modules).append("\n");
			}
			if(module_constructor_beans.length() > 0) {
				moduleClass.append("\n").append(module_constructor_beans).append("\n");
			}
			
			moduleClass.append(context.indent(1)).append("}\n");
			
			if(module_method_beans.length() > 0) {
				moduleClass.append("\n").append(module_method_beans).append("\n");
			}
			
			moduleClass.append(module_builder).append("\n\n");
			moduleClass.append(module_linker);
			
			moduleClass.append("\n}\n");
			
			return moduleClass;
		}
		else if(context.getMode() == GenerationMode.MODULE_BUILDER_CLASS) {
			TypeMirror moduleBuilderType = context.getTypeUtils().erasure(context.getElementUtils().getTypeElement(WINTER_CORE_MODULE_MODULEBUILDER_CLASS).asType());
			
			StringBuilder module_builder_fields = Arrays.stream(moduleInfo.getSockets()) 
				.filter(socketInfo -> socketInfo.isWired())
				.map(socketInfo -> this.visit(socketInfo, context.withModule(moduleInfo.getQualifiedName()).withMode(GenerationMode.SOCKET_FIELD)))
				.collect(context.joining("\n"));
			
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
				.collect(context.joining("\n"));
			
			StringBuilder module_builder_build_args = Arrays.stream(moduleInfo.getSockets())
				.filter(socketInfo -> socketInfo.isWired())
				.map(socketInfo -> new StringBuilder().append("this.").append(socketInfo.getQualifiedName().normalize()))
				.collect(context.joining(", "));
			
			StringBuilder module_builder_socket_methods = Arrays.stream(moduleInfo.getSockets())
				.filter(moduleSocketInfo -> moduleSocketInfo.isOptional())
				.filter(socketInfo -> socketInfo.isWired())
				.map(socketInfo -> this.visit(socketInfo, context.withModule(moduleInfo.getQualifiedName()).withMode(GenerationMode.SOCKET_INJECTOR)))
				.collect(context.joining("\n\n"));

			StringBuilder moduleBuilderClass = new StringBuilder().append(context.indent(1)).append("public static class Builder extends ").append(context.getTypeName(moduleBuilderType)).append("<").append(className).append("> {\n\n");
			if(module_builder_fields.length() > 0) {
				moduleBuilderClass.append(module_builder_fields).append("\n\n");
			}
			if(module_builder_constructor_parameters.length() > 0) {
				moduleBuilderClass.append(context.indent(2)).append("public Builder(").append(module_builder_constructor_parameters).append(") {\n");
				moduleBuilderClass.append(context.indent(3)).append("super(").append(module_builder_constructor_super_args).append(");\n\n");
				moduleBuilderClass.append(module_builder_constructor_assignments).append("\n");
				moduleBuilderClass.append(context.indent(2)).append("}\n\n");
			}
			
			moduleBuilderClass.append(context.indent(2)).append("protected ").append(className).append(" doBuild() {\n");
			moduleBuilderClass.append(context.indent(3)).append("return new ").append(className).append("(").append(module_builder_build_args).append(");\n");
			moduleBuilderClass.append(context.indent(2)).append("}\n");
			
			if(module_builder_socket_methods.length() > 0) {
				moduleBuilderClass.append("\n").append(module_builder_socket_methods).append("\n");
			}
			
			moduleBuilderClass.append(context.indent(1)).append("}");
			
			return moduleBuilderClass;
		}
		else if(context.getMode() == GenerationMode.MODULE_LINKER_CLASS) {
			TypeMirror moduleLinkerType = context.getTypeUtils().erasure(context.getElementUtils().getTypeElement(WINTER_CORE_MODULE_LINKER_CLASS).asType());
			TypeMirror mapType = context.getTypeUtils().erasure(context.getElementUtils().getTypeElement(Map.class.getCanonicalName()).asType());
			TypeMirror optionalType = context.getTypeUtils().erasure(context.getElementUtils().getTypeElement(Optional.class.getCanonicalName()).asType());
			
			StringBuilder linker_module_args = Arrays.stream(moduleInfo.getSockets())
				.filter(socketInfo -> socketInfo.isWired())
				.map(socketInfo -> {
					StringBuilder result = new StringBuilder().append(context.indent(4));
					if(socketInfo.isOptional()) {
						result.append("(").append(context.getTypeName(optionalType) +"<").append(context.getTypeName(context.getSupplierSocketType(socketInfo.getSocketType()))).append(">)");
					}
					else {
						result.append("(").append(context.getTypeName(context.getSupplierSocketType(socketInfo.getSocketType()))).append(")");
					}
					result.append("this.sockets.get(\"").append(socketInfo.getQualifiedName().normalize()).append("\")");

					return result ;
				})
				.collect(context.joining(",\n"));

			StringBuilder linkerClass = new StringBuilder().append(context.indent(1)).append("public static class Linker extends ").append(context.getTypeName(moduleLinkerType)).append("<").append(className).append("> {").append("\n\n");
			
			linkerClass.append(context.indent(2)).append("public Linker(").append(context.getTypeName(mapType)).append("<String, Object> sockets) {").append("\n");
			linkerClass.append(context.indent(3)).append("super(sockets);\n");
			linkerClass.append(context.indent(2)).append("}\n\n");

			
			linkerClass.append(context.indent(2)).append("@SuppressWarnings(\"unchecked\")\n");
			linkerClass.append(context.indent(2)).append("protected ").append(className).append(" link() {\n");
			linkerClass.append(context.indent(3)).append("return new ").append(className).append("(\n");
			linkerClass.append(linker_module_args).append("\n");
			linkerClass.append(context.indent(3)).append(");\n");
			linkerClass.append(context.indent(2)).append("}\n");
			
			linkerClass.append(context.indent(1)).append("}");
			
			return linkerClass;
		}
		else if(context.getMode() == GenerationMode.COMPONENT_MODULE_NEW) {
			TypeMirror componentModuleType = context.getElementUtils().getTypeElement(moduleInfo.getQualifiedName().getClassName()).asType();
			TypeMirror mapType = context.getTypeUtils().erasure(context.getElementUtils().getTypeElement(Map.class.getCanonicalName()).asType());

			StringBuilder component_module_arguments = Arrays.stream(moduleInfo.getSockets())
				.filter(socketInfo -> socketInfo.isWired())
				.map(socketInfo -> {
					StringBuilder ret = new StringBuilder().append(context.indent(3)).append(context.getTypeName(mapType)).append(".entry(\"").append(socketInfo.getQualifiedName().normalize()).append("\", ");
					ret.append(this.visit(socketInfo, context.withMode(GenerationMode.COMPONENT_MODULE_BEAN_REFERENCE).withIndentDepth(4)));
					ret.append(")");
					return ret;
				})
				.collect(context.joining(",\n"));
			
			StringBuilder moduleNew = new StringBuilder().append(context.indent(2)).append("this.").append(moduleInfo.getQualifiedName().normalize()).append(" = this.with(new ").append(context.getTypeName(componentModuleType)).append(".Linker(");
			if(component_module_arguments.length() > 0) {
				moduleNew.append(context.getTypeName(mapType)).append(".ofEntries(\n");
				moduleNew.append(component_module_arguments);
				moduleNew.append("\n").append(context.indent(2)).append(")");
			}
			else {
				moduleNew.append(context.getTypeName(mapType)).append(".of()");
			}
			moduleNew.append("));");
			
			return moduleNew;
		}
		else if(context.getMode() == GenerationMode.COMPONENT_MODULE_FIELD) {
			TypeMirror componentModuleType = context.getElementUtils().getTypeElement(moduleInfo.getQualifiedName().getClassName()).asType();
			return new StringBuilder().append(context.indent(1)).append("private ").append(context.getTypeName(componentModuleType)).append(" ").append(moduleInfo.getQualifiedName().normalize()).append(";");
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
			TypeMirror optionalType = context.getTypeUtils().erasure(context.getElementUtils().getTypeElement(Optional.class.getCanonicalName()).asType());
			TypeMirror npeType = context.getElementUtils().getTypeElement(NullPointerException.class.getCanonicalName()).asType();
			
			return new StringBuilder().append(context.getTypeName(optionalType)).append(".ofNullable(").append(this.visit(nestedBeanInfo.getProvidingBean(), context)).append(".").append(nestedBeanInfo.getName()).append("()).orElseThrow(() -> new ").append(context.getTypeName(npeType)).append("(\"").append(nestedBeanInfo.getQualifiedName().getSimpleValue()).append("\"))");
		}
		return new StringBuilder();
	}

	@Override
	public StringBuilder visit(ModuleBeanInfo moduleBeanInfo, ModuleClassGenerationContext context) {
		if(context.getMode() == GenerationMode.BEAN_FIELD) {
			TypeMirror moduleBeanType = context.getTypeUtils().getDeclaredType(context.getElementUtils().getTypeElement(WINTER_CORE_MODULE_BEAN_CLASS), moduleBeanInfo.getType());
			return new StringBuilder().append(context.indent(1)).append("private ").append(context.getTypeName(moduleBeanType)).append(" ").append(moduleBeanInfo.getQualifiedName().normalize()).append(";");
		}
		else if(context.getMode() == GenerationMode.BEAN_ACCESSOR) {
			StringBuilder beanAccessor = new StringBuilder();
			TypeMirror type = moduleBeanInfo.getProvidedType() != null ? moduleBeanInfo.getProvidedType() : moduleBeanInfo.getType(); 
			beanAccessor.append(context.indent(1)).append(moduleBeanInfo.getVisibility().equals(Bean.Visibility.PUBLIC) ? "public " : "private ").append(context.getTypeName(type)).append(" ").append(moduleBeanInfo.getQualifiedName().normalize()).append("() {\n");
			beanAccessor.append(context.indent(2)).append("return this.").append(moduleBeanInfo.getQualifiedName().normalize()).append(".get()").append(";\n");
			beanAccessor.append(context.indent(1)).append("}\n");
			
			return beanAccessor;
		}
		else if(context.getMode() == GenerationMode.BEAN_NEW) {
			if(moduleBeanInfo instanceof OverridableBeanInfo) {
				((OverridableBeanInfo)moduleBeanInfo).getOverridableBean();
				((OverridableBeanInfo)moduleBeanInfo).getOverridingSocket();
				StringBuilder beanNew = this.visit(((OverridableBeanInfo)moduleBeanInfo).getOverridableBean(), context);
				beanNew = beanNew.delete(beanNew.length() - 2, beanNew.length());
				
				beanNew.append(context.indent(1)).append(".override(").append(((OverridableBeanInfo)moduleBeanInfo).getOverridingSocket().getQualifiedName().normalize()).append("Override)\n");
				beanNew.append(context.indent(2)).append(");");
				
				return beanNew;
			}
			else {
				String variable = moduleBeanInfo.getQualifiedName().normalize();
				
				TypeMirror beanType;
				TypeMirror beanBuilderType;
				if(moduleBeanInfo instanceof WrapperBeanInfo) {
					beanType = ((WrapperBeanInfo)moduleBeanInfo).getWrapperType();
					beanBuilderType = context.getTypeUtils().erasure(context.getElementUtils().getTypeElement(WINTER_CORE_MODULE_WRAPPERBEANBUILDER_CLASS).asType());
				}
				else {
					beanType = moduleBeanInfo.getType();
					beanBuilderType = context.getTypeUtils().erasure(context.getElementUtils().getTypeElement(WINTER_CORE_MODULE_MODULEBEANBUILDER_CLASS).asType());
				}
				
				StringBuilder beanNew = new StringBuilder().append(context.indent(2)).append("this.").append(variable).append(" = this.with(").append(context.getTypeName(beanBuilderType)).append("\n");
				
				if(moduleBeanInfo.getStrategy().equals(Bean.Strategy.SINGLETON)) {
					beanNew.append(context.indent(3)).append(".singleton(\"").append(moduleBeanInfo.getQualifiedName().getSimpleValue()).append("\", () -> {\n");
				}
				else if(moduleBeanInfo.getStrategy().equals(Bean.Strategy.PROTOTYPE)) {
					beanNew.append(context.indent(3)).append(".prototype(\"").append(moduleBeanInfo.getQualifiedName().getSimpleValue()).append("\", () -> {\n");
				}
				else {
					throw new IllegalArgumentException("Unkown bean strategy: " + moduleBeanInfo.getStrategy());
				}
				
				beanNew.append(context.indent(4)).append(context.getTypeName(beanType)).append(" ").append(variable).append(" = new ").append(context.getTypeName(beanType)).append("(");
				if(moduleBeanInfo.getRequiredSockets().length > 0) {
					beanNew.append("\n");
					beanNew.append(Arrays.stream(moduleBeanInfo.getRequiredSockets())
						.sorted(new Comparator<ModuleBeanSocketInfo>() {
							public int compare(ModuleBeanSocketInfo s1, ModuleBeanSocketInfo s2) {
								if(s1.getSocketElement().get() != s2.getSocketElement().get()) {
									throw new IllegalStateException("Comparing required sockets with different socket elements");
								}
								List<String> orderedDependencyNames = s1.getSocketElement().get().getParameters().stream().map(element -> element.getSimpleName().toString()).collect(Collectors.toList());
								return orderedDependencyNames.indexOf(s1.getQualifiedName().getSimpleValue()) - orderedDependencyNames.indexOf(s2.getQualifiedName().getSimpleValue());
							}
						})
						.map(socketInfo -> new StringBuilder().append(context.indent(5)).append((socketInfo.isLazy() ? "() -> " : "")).append(this.visit(socketInfo, context.withMode(GenerationMode.BEAN_REFERENCE).withIndentDepth(5))))
						.collect(context.joining(", \n")));
					beanNew.append("\n").append(context.indent(4)).append(");\n");
				}
				else {
					beanNew.append(");\n");
				}
				// TODO: optionalSocket.ifPresent(bean::setXxx)
				beanNew.append(Arrays.stream(moduleBeanInfo.getOptionalSockets())
					.filter(socketInfo -> socketInfo.isResolved())
					.map(socketInfo -> new StringBuilder().append(context.indent(4)).append(variable).append(".").append(socketInfo.getSocketElement().get().getSimpleName().toString()).append("(").append((socketInfo.isLazy() ? "() -> " : "")).append(this.visit(socketInfo, context.withMode(GenerationMode.BEAN_REFERENCE).withIndentDepth(4))).append(");"))
					.collect(context.joining("\n"))).append("\n");
	
				beanNew.append(context.indent(4)).append("return ").append(variable).append(";\n");
				beanNew.append(context.indent(3)).append("})\n");
	
				if(moduleBeanInfo.getInitElements().length > 0) {
					beanNew.append(Arrays.stream(moduleBeanInfo.getInitElements())
						.map(element -> new StringBuilder().append(context.indent(3)).append(".init(").append(context.getTypeName(beanType)).append("::").append(element.getSimpleName().toString()).append(")"))
						.collect(context.joining("\n"))).append("\n");
				}
					
				if(moduleBeanInfo.getDestroyElements().length > 0) {
					beanNew.append(Arrays.stream(moduleBeanInfo.getDestroyElements())
						.map(element -> new StringBuilder().append(context.indent(3)).append(".destroy(").append(context.getTypeName(beanType)).append("::").append(element.getSimpleName().toString()).append(")"))
						.collect(context.joining("\n"))).append("\n");
				}	
	
				beanNew.append(context.indent(2)).append(");");
				
				return beanNew;
			}
		}
		else if(context.getMode() == GenerationMode.BEAN_REFERENCE) {
			if(moduleBeanInfo.getQualifiedName().getModuleQName().equals(context.getModule())) {
				// We can't use bean accessor for internal beans since provided types are ignored inside a module
				return new StringBuilder().append("this.").append(moduleBeanInfo.getQualifiedName().normalize()).append(".get()");
			}
			else {
				return new StringBuilder().append("this.").append(moduleBeanInfo.getQualifiedName().getModuleQName().normalize()).append(".").append(moduleBeanInfo.getQualifiedName().normalize()).append("()");
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
		if(context.getMode() == GenerationMode.BEAN_REFERENCE) {
			return this.visit(singleSocketInfo.getBean(), context);
		}
		return new StringBuilder();
	}

	@Override
	public StringBuilder visit(MultiSocketInfo multiSocketInfo, ModuleClassGenerationContext context) {
		if(!multiSocketInfo.isResolved()) {
			return new StringBuilder();
		}
		if(context.getMode() == GenerationMode.BEAN_REFERENCE) {
			final TypeMirror unwildDependencyType;
			if(multiSocketInfo.getType().getKind().equals(TypeKind.WILDCARD)) {
				if(((WildcardType)multiSocketInfo.getType()).getExtendsBound() != null) {
					unwildDependencyType = ((WildcardType)multiSocketInfo.getType()).getExtendsBound();
				}
				else if(((WildcardType)multiSocketInfo.getType()).getSuperBound() != null) {
					// TODO test it I don't know precisely what will happen here
					unwildDependencyType = ((WildcardType)multiSocketInfo.getType()).getSuperBound();
				}
				else {
					unwildDependencyType = context.getElementUtils().getTypeElement(Object.class.getCanonicalName()).asType();
				}
			}
			else {
				unwildDependencyType = multiSocketInfo.getType();
			}
			
			TypeMirror beanAggregatorType = context.getTypeUtils().erasure(context.getElementUtils().getTypeElement(WINTER_CORE_MODULE_BEANAGGREGATOR_CLASS).asType());
			
			StringBuilder beanSocketReference = new StringBuilder().append("new ").append(context.getTypeName(beanAggregatorType)).append("<").append(context.getTypeName(unwildDependencyType)).append(">()\n");
			beanSocketReference.append(Arrays.stream(multiSocketInfo.getBeans())
				.map(beanInfo -> new StringBuilder(context.indent(1)).append(".add(").append(this.visit(beanInfo, context)).append(")"))
				.collect(context.joining("\n"))).append("\n");
			
			if(multiSocketInfo.getMultiType().equals(MultiSocketType.ARRAY)) {
				beanSocketReference.append(context.indent(0)).append(".toArray(").append(context.getTypeName(unwildDependencyType)).append("[]::new)");
			}
			else if(multiSocketInfo.getMultiType().equals(MultiSocketType.COLLECTION) || multiSocketInfo.getMultiType().equals(MultiSocketType.LIST)) {
				beanSocketReference.append(context.indent(0)).append(".toList()");
			}
			else if(multiSocketInfo.getMultiType().equals(MultiSocketType.SET)) {
				beanSocketReference.append(context.indent(0)).append(".toSet()");
			}
			
			return beanSocketReference;
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
				TypeMirror socketAnnotationType = context.getElementUtils().getTypeElement(WINTER_CORE_MODULE_SOCKET_ANNOTATION).asType();
				
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
				TypeMirror optionalType = context.getTypeUtils().erasure(context.getElementUtils().getTypeElement(Optional.class.getCanonicalName()).asType());
				socketParameter.append(context.getTypeName(optionalType)).append("<").append(context.getTypeName(context.getSupplierSocketType(socketBeanInfo.getSocketType()))).append(">");
			}
			else {
				socketParameter.append(context.getTypeName(context.getSupplierSocketType(socketBeanInfo.getSocketType())));
			}
			
			socketParameter.append(" ").append(socketBeanInfo.getQualifiedName().normalize());
			if(socketBeanInfo instanceof OverridingSocketBeanInfo) {
				socketParameter.append("Override");
			}
			
			return socketParameter;
		}
		else if(context.getMode() == GenerationMode.SOCKET_FIELD) {
			TypeMirror optionalType = context.getTypeUtils().erasure(context.getElementUtils().getTypeElement(Optional.class.getCanonicalName()).asType());
			if(socketBeanInfo.isOptional()) {
				return new StringBuilder().append(context.indent(2)).append("private ").append(context.getTypeName(optionalType)).append("<").append(context.getTypeName(context.getSupplierSocketType(socketBeanInfo.getSocketType()))).append("> ").append(socketBeanInfo.getQualifiedName().normalize()).append(" = ").append(context.getTypeName(optionalType)).append(".empty();");
			}
			else {
				return new StringBuilder().append(context.indent(2)).append("private ").append(context.getTypeName(context.getSupplierSocketType(socketBeanInfo.getSocketType()))).append(" ").append(socketBeanInfo.getQualifiedName().normalize()).append(";");
			}
		}
		else if(context.getMode() == GenerationMode.SOCKET_ASSIGNMENT) {
			return new StringBuilder().append(context.indent(3)).append("this.").append(socketBeanInfo.getQualifiedName().normalize()).append(" = () -> ").append(socketBeanInfo.getQualifiedName().normalize()).append(";");
		}
		else if(context.getMode() == GenerationMode.SOCKET_INJECTOR) {
			TypeMirror optionalType = context.getTypeUtils().erasure(context.getElementUtils().getTypeElement(Optional.class.getCanonicalName()).asType());
			StringBuilder plugName = new StringBuilder().append(socketBeanInfo.getQualifiedName().normalize());
			
			StringBuilder result = new StringBuilder().append(context.indent(2)).append("public Builder set").append(Character.toUpperCase(plugName.charAt(0))).append(plugName.substring(1)).append("(").append((socketBeanInfo instanceof MultiSocketInfo ? context.getMultiTypeName(socketBeanInfo.getType(), ((MultiSocketInfo)socketBeanInfo).getMultiType() ) : context.getTypeName(socketBeanInfo.getType()))).append(" ").append(plugName).append(") {\n");
			if(socketBeanInfo.isOptional()) {
				result.append(context.indent(3)).append("this.").append(plugName).append(" = ").append(context.getTypeName(optionalType)).append(".ofNullable(").append(plugName).append(" != null ? () -> ").append(plugName).append(" : null);\n");
			}
			else {
				result.append(context.indent(3)).append("this.").append(plugName).append(" = () -> ").append(plugName).append(";\n");
			}
			result.append(context.indent(3)).append("return this;\n");
			result.append(context.indent(2)).append("}");
			
			return result;
		}
		else if(context.getMode() == GenerationMode.BEAN_REFERENCE) {
			if(socketBeanInfo.isOptional()) {
				return new StringBuilder().append(socketBeanInfo.getQualifiedName().normalize()).append(".orElse(() -> null).get()");
			}
			else {
				return new StringBuilder().append(socketBeanInfo.getQualifiedName().normalize()).append(".get()");
			}
		}
		else if(context.getMode() == GenerationMode.COMPONENT_MODULE_BEAN_REFERENCE) {
			TypeMirror optionalType = context.getTypeUtils().erasure(context.getElementUtils().getTypeElement(Optional.class.getCanonicalName()).asType());
			if(!socketBeanInfo.isOptional() || socketBeanInfo.isResolved()) {
				StringBuilder result = new StringBuilder();
				if(socketBeanInfo instanceof SingleSocketInfo) {
					result.append(this.visit((SingleSocketBeanInfo)socketBeanInfo, context));
				}
				else if(socketBeanInfo instanceof MultiSocketInfo) {
					result.append(this.visit((MultiSocketBeanInfo)socketBeanInfo, context));
				}
				
				if(socketBeanInfo.isOptional()) {
					return new StringBuilder().append(context.getTypeName(optionalType)).append(".of(").append(result).append(")");
				}
				return result;
			}
			else {
				return new StringBuilder().append(context.getTypeName(optionalType)).append(".empty()");
			}
		}
		return new StringBuilder();
	}

	@Override
	public StringBuilder visit(SingleSocketBeanInfo singleSocketBeanInfo, ModuleClassGenerationContext context) {
		if(context.getMode() == GenerationMode.COMPONENT_MODULE_BEAN_REFERENCE) {
			StringBuilder result = new StringBuilder().append("(").append(context.getTypeName(context.getSupplierSocketType(singleSocketBeanInfo.getSocketType()))).append(")");
			if(singleSocketBeanInfo.isResolved()) {
				result.append("() -> ").append(this.visit((SingleSocketInfo)singleSocketBeanInfo, context.withMode(GenerationMode.BEAN_REFERENCE)));
			}
			else {
				result.append("null");
			}
			return result;
		}
		return new StringBuilder();
	}

	@Override
	public StringBuilder visit(MultiSocketBeanInfo multiSocketBeanInfo, ModuleClassGenerationContext context) {
		if(context.getMode() == GenerationMode.COMPONENT_MODULE_BEAN_REFERENCE) {
			StringBuilder result = new StringBuilder().append("(").append(context.getTypeName(context.getSupplierSocketType(multiSocketBeanInfo.getSocketType()))).append(")");
			if(multiSocketBeanInfo.isResolved()) {
				result.append("() -> ").append(this.visit((MultiSocketInfo)multiSocketBeanInfo, context.withMode(GenerationMode.BEAN_REFERENCE)));
			}
			else {
				result.append("null");
			}
			return result;
		}
		return new StringBuilder();
	}
}
