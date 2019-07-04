/**
 * 
 */
package io.winterframework.core.compiler;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Collectors;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.ModuleElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import javax.tools.Diagnostic.Kind;

import io.winterframework.core.annotation.Bean;
import io.winterframework.core.annotation.Module;
import io.winterframework.core.compiler.bean.ModuleBeanInfoFactory;
import io.winterframework.core.compiler.module.ModuleInfoBuilderFactory;
import io.winterframework.core.compiler.socket.SocketBeanInfoFactory;
import io.winterframework.core.compiler.spi.ModuleBeanInfo;
import io.winterframework.core.compiler.spi.ModuleInfoBuilder;
import io.winterframework.core.compiler.spi.SocketBeanInfo;

/**
 * @author jkuhn
 *
 */
@SupportedAnnotationTypes({"javax.annotation.processing.Generated", "io.winterframework.core.annotation/io.winterframework.core.annotation.Module","io.winterframework.core.annotation/io.winterframework.core.annotation.Bean"})
public class ModuleAnnotationProcessor extends AbstractProcessor {

	private ModuleGenerator moduleGenerator;
	
	@Override
	public SourceVersion getSupportedSourceVersion() {
		return this.processingEnv.getSourceVersion().compareTo(SourceVersion.RELEASE_9) < 0 ? SourceVersion.RELEASE_9 : this.processingEnv.getSourceVersion();
	}
	
	@Override
	public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
		if(this.moduleGenerator == null) {
			TypeMirror moduleAnnotationType = this.processingEnv.getElementUtils().getTypeElement(Module.class.getCanonicalName()).asType();
			TypeMirror beanAnnotationType = this.processingEnv.getElementUtils().getTypeElement(Bean.class.getCanonicalName()).asType();
		
			Map<String, List<Element>> moduleOriginatingElements = new HashMap<>();
		
			Map<String, ModuleInfoBuilder> moduleBuilders = new TreeMap<>(Collections.reverseOrder());
			Map<String, SocketBeanInfoFactory> socketFactories = new TreeMap<>(Collections.reverseOrder());
			Map<String, ModuleBeanInfoFactory> beanFactories = new TreeMap<>(Collections.reverseOrder());
		
			roundEnv.getElementsAnnotatedWith(Module.class).stream()
				.forEach(element -> {
					String moduleName = ((ModuleElement)element).getQualifiedName().toString();
					beanFactories.put(moduleName, ModuleBeanInfoFactory.create(this.processingEnv, (ModuleElement)element));
					socketFactories.put(moduleName, SocketBeanInfoFactory.create(this.processingEnv, (ModuleElement)element));
					
					moduleBuilders.put(moduleName, ModuleInfoBuilderFactory.createModuleBuilder(this.processingEnv, (ModuleElement)element));
					if(!moduleOriginatingElements.containsKey(moduleName)) {
						moduleOriginatingElements.put(moduleName, new ArrayList<>());
					}
					moduleOriginatingElements.get(moduleName).add(element);
				});
			
			this.moduleGenerator = new ModuleGenerator(this.processingEnv)
				.forModules(moduleBuilders)
				.withOriginatingElements(moduleOriginatingElements)
				.withModuleBeans(roundEnv.getElementsAnnotatedWith(Bean.class).stream()
					.filter(element -> element.getKind().equals(ElementKind.CLASS))
					.map(element -> {
						ModuleBeanInfoFactory beanFactory = beanFactories.get(((ModuleElement)element.getEnclosingElement().getEnclosingElement()).getQualifiedName().toString());
						if(beanFactory == null) {
							AnnotationMirror beanAnnotation = element.getAnnotationMirrors().stream().filter(a -> this.processingEnv.getTypeUtils().isSameType(a.getAnnotationType(), beanAnnotationType)).findFirst().get();
							this.processingEnv.getMessager().printMessage(Kind.MANDATORY_WARNING, "Bean might be out of sync with the module please consider recompiling the module" , element, beanAnnotation );
							return null;
						}
						
						ModuleBeanInfo moduleBean = beanFactory.createBean(element);
						moduleOriginatingElements.get(moduleBean.getQualifiedName().getModuleQName().getValue()).add(element);
						return moduleBean;
					})
					.filter(Objects::nonNull)
					.collect(Collectors.groupingBy(moduleBean -> moduleBean.getQualifiedName().getModuleQName().getValue())))
				.withModuleSockets(roundEnv.getElementsAnnotatedWith(Bean.class).stream()
					.filter(element -> element.getKind().equals(ElementKind.INTERFACE))
					.map(element -> {
						SocketBeanInfoFactory socketFactory = socketFactories.get(((ModuleElement)element.getEnclosingElement().getEnclosingElement()).getQualifiedName().toString());
						if(socketFactory == null) {
							AnnotationMirror beanAnnotation = element.getAnnotationMirrors().stream().filter(a -> this.processingEnv.getTypeUtils().isSameType(a.getAnnotationType(), beanAnnotationType)).findFirst().get();
							this.processingEnv.getMessager().printMessage(Kind.MANDATORY_WARNING, "Module socket bean might be out of sync with the module please consider recompiling the module" , element, beanAnnotation );
							return null;
						}
						SocketBeanInfo moduleSocket = socketFactory.createModuleSocket(element);
						moduleOriginatingElements.get(moduleSocket.getQualifiedName().getModuleQName().getValue()).add(element);
						return moduleSocket;
					})
					.filter(socket -> socket != null)
					.collect(Collectors.groupingBy(socket -> socket.getQualifiedName().getModuleQName().getValue())))
				.withImportedModules(roundEnv.getElementsAnnotatedWith(Module.class).stream()
					.collect(Collectors.toMap(
						element -> ((ModuleElement)element).getQualifiedName().toString(), 
						element -> {
							ModuleElement moduleElement = (ModuleElement)element;
					
							return moduleElement.getDirectives().stream()
								.filter(directive -> {
									if(!directive.getKind().equals(ModuleElement.DirectiveKind.REQUIRES)) {
										return false;
									}
									ModuleElement importedModuleElement = ((ModuleElement.RequiresDirective)directive).getDependency();
									Optional<? extends AnnotationMirror> moduleAnnotation = this.processingEnv.getElementUtils().getAllAnnotationMirrors(importedModuleElement).stream().filter(a -> this.processingEnv.getTypeUtils().isSameType(a.getAnnotationType(), moduleAnnotationType)).findFirst();
									return moduleAnnotation.isPresent();
								})
								.map(directive -> {
									ModuleElement importedModuleElement = ((ModuleElement.RequiresDirective)directive).getDependency();
									String importedModuleName = importedModuleElement.getQualifiedName().toString();
									if(moduleBuilders.containsKey(importedModuleName)) {
										return moduleBuilders.get(importedModuleName);
									}
								
									ModuleInfoBuilder importedModuleBuilder = ModuleInfoBuilderFactory.createModuleBuilder(this.processingEnv, moduleElement, importedModuleElement);
									TypeElement moduleType = this.processingEnv.getElementUtils().getTypeElement(importedModuleBuilder.getQualifiedName().getClassName());
									
									SocketBeanInfoFactory importedModuleSocketFactory = SocketBeanInfoFactory.create(this.processingEnv, moduleElement, importedModuleElement);
								
									List<? extends SocketBeanInfo> importedModuleSockets = ((ExecutableElement)moduleType.getEnclosedElements().stream()
										.filter(e -> e.getKind().equals(ElementKind.CONSTRUCTOR)).findFirst().get())
										.getParameters().stream()
										.map(ve -> importedModuleSocketFactory.createModuleSocket(ve))
										.collect(Collectors.toList());
	
									importedModuleBuilder.sockets(importedModuleSockets.stream().toArray(SocketBeanInfo[]::new));
	
									ModuleBeanInfoFactory importedModuleBeanFactory = ModuleBeanInfoFactory.create(this.processingEnv, moduleElement, importedModuleElement, importedModuleSockets);
									List<? extends ModuleBeanInfo> importedModuleBeans = moduleType.getEnclosedElements().stream()
										.filter(e -> e.getKind().equals(ElementKind.METHOD) && e.getModifiers().contains(Modifier.PUBLIC) && ((ExecutableElement)e).getParameters().size() == 0)
										.map(e -> importedModuleBeanFactory.createBean(e))
										.collect(Collectors.toList());
							
									importedModuleBuilder.beans(importedModuleBeans.stream().toArray(ModuleBeanInfo[]::new));
								
									return importedModuleBuilder;
								})
								.collect(Collectors.toList());
						}))
				);
		}
		this.moduleGenerator.generateNextRound();
		return true;
	}
}
