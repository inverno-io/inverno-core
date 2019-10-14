/**
 * 
 */
package io.winterframework.core.compiler.socket;

import java.util.Collections;
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
import javax.lang.model.element.ModuleElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;

import io.winterframework.core.annotation.Bean;
import io.winterframework.core.annotation.Selector;
import io.winterframework.core.annotation.WiredTo;
import io.winterframework.core.compiler.TypeErrorException;
import io.winterframework.core.compiler.spi.BeanQualifiedName;
import io.winterframework.core.compiler.spi.MultiSocketType;

/**
 * @author jkuhn
 *
 */
class ImportedSocketBeanInfoFactory extends SocketBeanInfoFactory {

	private ModuleElement compiledModuleElement;
	
	private TypeMirror supplierType;
	private TypeMirror optionalType;
	
	private TypeMirror beanAnnotationType;
	
	private TypeMirror wiredToAnnotationType;
	
	/**
	 * @param processingEnvironment
	 * @param moduleElement
	 */
	public ImportedSocketBeanInfoFactory(ProcessingEnvironment processingEnvironment, ModuleElement moduleElement, ModuleElement compiledModuleElement) {
		super(processingEnvironment, moduleElement);
		
		this.compiledModuleElement = compiledModuleElement;
		this.supplierType = this.processingEnvironment.getTypeUtils().erasure(this.processingEnvironment.getElementUtils().getTypeElement(Supplier.class.getCanonicalName()).asType());
		this.optionalType = this.processingEnvironment.getTypeUtils().erasure(this.processingEnvironment.getElementUtils().getTypeElement(Optional.class.getCanonicalName()).asType());
		this.beanAnnotationType = this.processingEnvironment.getElementUtils().getTypeElement(Bean.class.getCanonicalName()).asType();
		this.wiredToAnnotationType = this.processingEnvironment.getElementUtils().getTypeElement(WiredTo.class.getCanonicalName()).asType();
	}

	@Override
	@SuppressWarnings("unchecked")
	public WirableSocketBeanInfo createModuleSocket(Element element) throws SocketCompilationException, TypeErrorException {
		if(!element.getKind().equals(ElementKind.PARAMETER)) {
			throw new IllegalArgumentException("Element must be a parameter");
		}
		
		VariableElement variableElement = (VariableElement)element;
		ExecutableElement socketElement = (ExecutableElement)variableElement.getEnclosingElement();
		if(!((TypeElement)socketElement.getEnclosingElement()).getQualifiedName().toString().equals(this.moduleQName.getClassName())) {
			throw new IllegalArgumentException("The specified element doesn't belong to module " + this.moduleQName);
		}
		
		boolean optional = false;
		TypeElement moduleSocketElement = (TypeElement)this.processingEnvironment.getTypeUtils().asElement(variableElement.asType());
		if(this.processingEnvironment.getTypeUtils().isSameType(this.processingEnvironment.getTypeUtils().erasure(variableElement.asType()), this.optionalType)) {
			optional = true;
			moduleSocketElement = (TypeElement)this.processingEnvironment.getTypeUtils().asElement(((DeclaredType)variableElement.asType()).getTypeArguments().get(0));
		}
		
		Optional<? extends AnnotationMirror> annotation = this.processingEnvironment.getElementUtils().getAllAnnotationMirrors(moduleSocketElement).stream().filter(a -> this.processingEnvironment.getTypeUtils().isSameType(a.getAnnotationType(), this.beanAnnotationType)).findFirst();
		if(!annotation.isPresent()) {
			throw new IllegalArgumentException("The specified element is not annotated with " + Bean.class.getSimpleName());
		}

		if(moduleSocketElement.getKind() != ElementKind.INTERFACE) {
			throw new IllegalArgumentException("A socket bean element must be an interface");
		}
		
		String socketName = null;
		Bean.Visibility visibility = null;
		for(Entry<? extends ExecutableElement, ? extends AnnotationValue> value : this.processingEnvironment.getElementUtils().getElementValuesWithDefaults(annotation.get()).entrySet()) {
			switch(value.getKey().getSimpleName().toString()) {
				case "name" : socketName = (String)value.getValue().getValue();
					break;
				case "visibility" : visibility = Bean.Visibility.valueOf(value.getValue().getValue().toString());
					break;
			}
		}
		
		if(visibility.equals(Bean.Visibility.PRIVATE)) {
			throw new IllegalArgumentException("A socket bean must be public");
		}
		
		Optional<? extends TypeMirror> supplierType = moduleSocketElement.getInterfaces().stream().filter(t -> this.processingEnvironment.getTypeUtils().isSameType(this.processingEnvironment.getTypeUtils().erasure(t), this.supplierType)).findFirst();
		if(!supplierType.isPresent()) {
			throw new IllegalArgumentException("A socket element must extend " + Supplier.class.getCanonicalName());
		}
		
		TypeMirror beanType = null;
		if(((DeclaredType)supplierType.get()).getTypeArguments().size() == 0) {
			beanType = this.processingEnvironment.getElementUtils().getTypeElement(Object.class.getCanonicalName()).asType();
		}
		else {
			beanType = ((DeclaredType)supplierType.get()).getTypeArguments().get(0);
		}
		
		// Socket name
		if(socketName == null || socketName.equals("")) {
			socketName = moduleSocketElement.getSimpleName().toString();
			socketName = Character.toLowerCase(socketName.charAt(0)) + socketName.substring(1);
		}

		// This should never throw a QualifiedNameFormatException as it should have already been tested when the module was compiled
		BeanQualifiedName socketQName = new BeanQualifiedName(this.moduleQName, socketName);

		AnnotationMirror[] selectors = element.getAnnotationMirrors().stream().filter(a -> a.getAnnotationType().asElement().getAnnotation(Selector.class) != null).toArray(AnnotationMirror[]::new);
		
		MultiSocketType multiType = this.getMultiType(beanType);
		final AbstractSocketBeanInfo moduleSocketInfo;
		// Use compiledModuleElement instead of moduleElement to report compilation errors on the compiled module 
		if(multiType != null) {
			moduleSocketInfo = new CommonMultiSocketBeanInfo(this.processingEnvironment, this.compiledModuleElement, socketQName, this.getComponentType(beanType), moduleSocketElement.asType(), socketElement, selectors, optional, multiType);
		}
		else {
			moduleSocketInfo = new CommonSingleSocketBeanInfo(this.processingEnvironment, this.compiledModuleElement, socketQName, beanType, moduleSocketElement.asType(), socketElement, selectors, optional);
		}
		
		moduleSocketInfo.setWiredBeans(variableElement.getAnnotationMirrors().stream()
			.filter(a -> this.processingEnvironment.getTypeUtils().isSameType(a.getAnnotationType(), this.wiredToAnnotationType))
			.flatMap(a -> {
				List<String> wiredBeanQNames = Collections.emptyList();			
				for(Entry<? extends ExecutableElement, ? extends AnnotationValue> value : this.processingEnvironment.getElementUtils().getElementValuesWithDefaults(a).entrySet()) {
					switch(value.getKey().getSimpleName().toString()) {
						case "value" : wiredBeanQNames = ((List<AnnotationValue>)value.getValue().getValue()).stream().map(v -> (String)v.getValue()).collect(Collectors.toList());
							break;
					}
				}
				return wiredBeanQNames.stream();
			})
			.map(name -> new BeanQualifiedName(this.moduleQName, name))
			.collect(Collectors.toSet()));
		
		return moduleSocketInfo;
	}

}
