/**
 * 
 */
package io.winterframework.core.compiler.bean;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.ModuleElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;
import javax.tools.Diagnostic.Kind;

import io.winterframework.core.annotation.Bean;
import io.winterframework.core.annotation.BeanSocket;
import io.winterframework.core.annotation.Destroy;
import io.winterframework.core.annotation.Init;
import io.winterframework.core.annotation.Provide;
import io.winterframework.core.annotation.Scope;
import io.winterframework.core.annotation.Wrapper;
import io.winterframework.core.compiler.TypeErrorException;
import io.winterframework.core.compiler.common.ReporterInfo;
import io.winterframework.core.compiler.spi.BeanQualifiedName;
import io.winterframework.core.compiler.spi.ModuleBeanInfo;
import io.winterframework.core.compiler.spi.ModuleBeanSocketInfo;
import io.winterframework.core.compiler.spi.QualifiedNameFormatException;

/**
 * @author jkuhn
 *
 */
class CompiledModuleBeanInfoFactory extends ModuleBeanInfoFactory {

	private TypeMirror beanAnnotationType;
	private TypeMirror provideAnnotationType;
	private TypeMirror scopeAnnotationType;
	private TypeMirror wrapperAnnotationType;
	private TypeMirror supplierType;
	
	/**
	 * @param processingEnvironment
	 * @param moduleElement
	 */
	CompiledModuleBeanInfoFactory(ProcessingEnvironment processingEnvironment, ModuleElement moduleElement) {
		super(processingEnvironment, moduleElement);
		
		this.beanAnnotationType = this.processingEnvironment.getElementUtils().getTypeElement(Bean.class.getCanonicalName()).asType();
		this.provideAnnotationType = this.processingEnvironment.getElementUtils().getTypeElement(Provide.class.getCanonicalName()).asType();
		this.scopeAnnotationType = this.processingEnvironment.getElementUtils().getTypeElement(Scope.class.getCanonicalName()).asType();
		this.wrapperAnnotationType = this.processingEnvironment.getElementUtils().getTypeElement(Wrapper.class.getCanonicalName()).asType();
		this.supplierType = this.processingEnvironment.getTypeUtils().erasure(this.processingEnvironment.getElementUtils().getTypeElement(Supplier.class.getCanonicalName()).asType());
	}

	/* (non-Javadoc)
	 * @see io.winterframework.core.compiler.model.ModuleBeanInfoFactory#createBean(javax.lang.model.element.Element)
	 */
	@Override
	public ModuleBeanInfo createBean(Element element) throws BeanCompilationException, TypeErrorException {
		if(!TypeElement.class.isAssignableFrom(element.getClass())) {
			throw new IllegalArgumentException("Element must be a TypeElement");
		}
		if(!element.getKind().equals(ElementKind.CLASS)) {
			throw new IllegalArgumentException("Element must be a Class");
		}
		
		TypeElement typeElement = (TypeElement)element;
		TypeMirror beanType = typeElement.asType();
		
		Optional<? extends AnnotationMirror> beanAnnotation = this.processingEnvironment.getElementUtils().getAllAnnotationMirrors(element).stream().filter(a -> this.processingEnvironment.getTypeUtils().isSameType(a.getAnnotationType(), this.beanAnnotationType)).findFirst();
		if(!beanAnnotation.isPresent()) {
			throw new IllegalArgumentException("The specified element is not annotated with " + Bean.class.getSimpleName());
		}
		if(!typeElement.getEnclosingElement().getEnclosingElement().equals(this.moduleElement)) {
			throw new IllegalArgumentException("The specified element doesn't belong to module " + this.moduleQName);
		}
		
		ReporterInfo beanReporter = this.getReporter(element, beanAnnotation.get());
		if(!element.getKind().equals(ElementKind.CLASS) || element.getModifiers().contains(Modifier.ABSTRACT)) {
			beanReporter.error("A module bean or a wrapper bean must be a concrete class");
			throw new BeanCompilationException();
		}
		
		// Get Bean metadata
		String name = null;
		Bean.Visibility visibility = null;
		for(Entry<? extends ExecutableElement, ? extends AnnotationValue> value : this.processingEnvironment.getElementUtils().getElementValuesWithDefaults(beanAnnotation.get()).entrySet()) {
			switch(value.getKey().getSimpleName().toString()) {
				case "name" : name = (String)value.getValue().getValue();
					break;
				case "visibility" : visibility = Bean.Visibility.valueOf(value.getValue().getValue().toString());
					break;
			}
		}
		
		// Bean qualified name
		if(name == null || name.equals("")) {
			name = element.getSimpleName().toString();
			name = Character.toLowerCase(name.charAt(0)) + name.substring(1);
		}
		
		BeanQualifiedName beanQName;
		try {
			beanQName = new BeanQualifiedName(this.moduleQName, name);
		} 
		catch (QualifiedNameFormatException e) {
			beanReporter.error("Invalid bean qualified name: " + e.getMessage());
			throw new BeanCompilationException();
		}

		Scope.Type scope = null;
		Optional<? extends AnnotationMirror> scopeAnnotation = this.processingEnvironment.getElementUtils().getAllAnnotationMirrors(element).stream().filter(a -> this.processingEnvironment.getTypeUtils().isSameType(a.getAnnotationType(), this.scopeAnnotationType)).findFirst();
		if(scopeAnnotation.isPresent()) {
			for(Entry<? extends ExecutableElement, ? extends AnnotationValue> value : this.processingEnvironment.getElementUtils().getElementValuesWithDefaults(scopeAnnotation.get()).entrySet()) {
				switch(value.getKey().getSimpleName().toString()) {
					case "value" : scope = Scope.Type.valueOf(value.getValue().getValue().toString());
						break;
				}
			}
		}

		// Get provided type
		TypeMirror providedType = null;
		List<? extends TypeMirror> providedTypes = this.processingEnvironment.getTypeUtils().directSupertypes(beanType).stream()
			.filter(superType -> superType.getAnnotationMirrors().stream().anyMatch(a -> this.processingEnvironment.getTypeUtils().isSameType(a.getAnnotationType(), this.provideAnnotationType)))
			.map(superType -> {
				TypeMirror[] superTypeArguments = ((DeclaredType)superType).getTypeArguments().stream().toArray(TypeMirror[]::new);
				TypeElement superTypeElement = (TypeElement)((DeclaredType)this.processingEnvironment.getTypeUtils().erasure(superType)).asElement();
				
				return this.processingEnvironment.getTypeUtils().getDeclaredType(superTypeElement, superTypeArguments);
			})
			.collect(Collectors.toList());
		
		if(providedTypes.size() == 1) {
			providedType = providedTypes.get(0);
		}
		else if(providedTypes.size() > 1) {
			beanReporter.error("Bean " + beanQName + " can't provide multiple types");
		}
		
		// Get Init
		List<ExecutableElement> initElements = element.getEnclosedElements().stream()
			.filter(e -> e.getAnnotation(Init.class) != null)
			.map(e -> (ExecutableElement)e)
			.filter(e -> {
				if(((ExecutableElement)e).getParameters().size() > 0) {
					this.processingEnvironment.getMessager().printMessage(Kind.MANDATORY_WARNING, "Invalid " + Init.class.getSimpleName() + " method which should be a no-argument method, it will be ignored", e);
					return false;
				}
				return true;
			}).collect(Collectors.toList());
		
		// Get Destroy
		List<ExecutableElement> destroyElements = element.getEnclosedElements().stream()
			.filter(e -> e.getAnnotation(Destroy.class) != null)
			.map(e -> (ExecutableElement)e)
			.filter(e -> {
				if(e.getParameters().size() > 0) {
					this.processingEnvironment.getMessager().printMessage(Kind.MANDATORY_WARNING, "Invalid " + Destroy.class.getSimpleName() + " method which should be a no-argument method, it will be ignored", e);
					return false;
				}
				return true;
			}).collect(Collectors.toList());
		
		// Get sockets...
		List<ModuleBeanSocketInfo> beanSocketInfos = new ArrayList<>();
		List<String> requiredSocketSimpleNames = new ArrayList<>();
		ModuleBeanSocketInfoFactory beanSocketFactory = ModuleBeanSocketInfoFactory.create(this.processingEnvironment, this.moduleElement, beanQName);
		
		// ... from Constructor
		ExecutableElement constructorSocketElement = null;
		
		List<ExecutableElement> constructorSocketElements = element.getEnclosedElements().stream()
			.filter(e -> e.getKind().equals(ElementKind.CONSTRUCTOR))
			.filter(e -> e.getModifiers().stream().anyMatch(m -> m.equals(Modifier.PUBLIC)))
			.map(e -> (ExecutableElement)e)
			.collect(Collectors.toList());
		if(constructorSocketElements.size() == 0) {
			// This should never happen
			beanReporter.error("No public constructor defined in bean " + beanQName);
			throw new BeanCompilationException();
		}
		else if(constructorSocketElements.size() == 1) {
			// OK
			constructorSocketElement = constructorSocketElements.get(0);
		}
		else {
			// multiple constructor
			constructorSocketElements = constructorSocketElements.stream().filter(e -> e.getAnnotation(BeanSocket.class) != null).collect(Collectors.toList());
			
			if(constructorSocketElements.size() == 0) {
				beanReporter.error("Multiple constructors are defined in module bean " + beanQName + ", consider specifying a " + BeanSocket.class.getSimpleName() + " on the one to select");
				throw new BeanCompilationException();
			}
			else if(constructorSocketElements.size() == 1) {
				constructorSocketElement = constructorSocketElements.get(0);
			}
			else {
				beanReporter.error("Multiple constructors annotated with " + BeanSocket.class.getSimpleName() + " are defined in module bean " + beanQName + " which is not permitted");
				throw new BeanCompilationException();
			}
		}
		
		for(VariableElement ve : constructorSocketElement.getParameters()) {
			ModuleBeanSocketInfo requiredBeanSocket = beanSocketFactory.createBeanSocket(ve);
			requiredSocketSimpleNames.add(requiredBeanSocket.getQualifiedName().getName());
			beanSocketInfos.add(requiredBeanSocket);
		}
		
		// ... from setters
		// Get optional dependencies from public setters
		List<ExecutableElement> socketElements = (List<ExecutableElement>)element.getEnclosedElements().stream()
			.filter(e -> e.getKind().equals(ElementKind.METHOD) && e.getSimpleName().toString().startsWith("set") && ((ExecutableElement)e).getParameters().size() == 1)
			.filter(e -> e.getModifiers().stream().anyMatch(m -> m.equals(Modifier.PUBLIC)))
			.map(e -> (ExecutableElement)e)
			.collect(Collectors.toList());
		List<ExecutableElement> annotatedSocketElements = socketElements.stream().filter(e -> e.getAnnotation(BeanSocket.class) != null).collect(Collectors.toList());
		if(annotatedSocketElements.size() > 0) {
			socketElements = annotatedSocketElements;
		}
		
		for(ExecutableElement socketElement : socketElements) {
			if(socketElement.getParameters().size() > 1) {
				this.processingEnvironment.getMessager().printMessage(Kind.MANDATORY_WARNING, "Invalid setter method which should be a single-argument method, socket will be ignored", socketElement);
			}
			else {
				ModuleBeanSocketInfo optionalBeanSocket =  beanSocketFactory.createBeanSocket(socketElement.getParameters().get(0));
				if(requiredSocketSimpleNames.contains(optionalBeanSocket.getQualifiedName().getName())) {
					optionalBeanSocket.error("Optional socket name is conflicting with a required socket name: " + optionalBeanSocket.getQualifiedName().getName());
				}
				else {
					beanSocketInfos.add(optionalBeanSocket);
				}
			}
		}
		
		Optional<? extends AnnotationMirror> wrapperAnnotation = this.processingEnvironment.getElementUtils().getAllAnnotationMirrors(element).stream().filter(a -> this.processingEnvironment.getTypeUtils().isSameType(a.getAnnotationType(), this.wrapperAnnotationType)).findFirst();
		TypeMirror wrapperType = null;
		if(wrapperAnnotation.isPresent()) {
			wrapperType = beanType;
			Optional<? extends TypeMirror> supplierType = typeElement.getInterfaces().stream().filter(t -> this.processingEnvironment.getTypeUtils().isSameType(this.processingEnvironment.getTypeUtils().erasure(t), this.supplierType)).findFirst();
			if(!supplierType.isPresent()) {
				beanReporter.error("A wrapper bean element must extend " + Supplier.class.getCanonicalName());
				throw new BeanCompilationException();
			}
			if(((DeclaredType)supplierType.get()).getTypeArguments().size() == 0) {
				beanType = this.processingEnvironment.getElementUtils().getTypeElement(Object.class.getCanonicalName()).asType();
			}
			else {
				beanType = ((DeclaredType)supplierType.get()).getTypeArguments().get(0);
			}
			
			if(providedType != null) {
				beanReporter.error("Wrapper bean " + beanQName + " can't provide other types than its supplied type");
				throw new BeanCompilationException();
			}
			return new CompiledWrapperBeanInfo(this.processingEnvironment, typeElement, beanAnnotation.get(), beanQName, wrapperType, beanType, visibility, scope, initElements, destroyElements, beanSocketInfos);
		}
		else {
			return new CommonModuleBeanInfo(this.processingEnvironment, typeElement, beanAnnotation.get(), beanQName, beanType, providedType, visibility, scope, initElements, destroyElements, beanSocketInfos);
		}
	}
}
