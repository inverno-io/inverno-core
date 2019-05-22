/**
 * 
 */
package io.winterframework.core.compiler.socket;

import java.util.Map.Entry;
import java.util.Optional;
import java.util.function.Supplier;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.ModuleElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;

import io.winterframework.core.annotation.Bean;
import io.winterframework.core.compiler.common.ReporterInfo;
import io.winterframework.core.compiler.spi.BeanQualifiedName;
import io.winterframework.core.compiler.spi.MultiSocketType;
import io.winterframework.core.compiler.spi.QualifiedNameFormatException;

/**
 * @author jkuhn
 *
 */
class CompiledSocketBeanInfoFactory extends SocketBeanInfoFactory {

//	private TypeMirror moduleSocketAnnotationType;
	private TypeMirror beanAnnotationType;
	private TypeMirror supplierType;
	
	/**
	 * @param processingEnvironment
	 * @param moduleElement
	 */
	public CompiledSocketBeanInfoFactory(ProcessingEnvironment processingEnvironment, ModuleElement moduleElement) {
		super(processingEnvironment, moduleElement);
		
//		this.moduleSocketAnnotationType = this.processingEnvironment.getElementUtils().getTypeElement(ModuleSocket.class.getCanonicalName()).asType();
		this.beanAnnotationType = this.processingEnvironment.getElementUtils().getTypeElement(Bean.class.getCanonicalName()).asType();
		this.supplierType = this.processingEnvironment.getTypeUtils().erasure(this.processingEnvironment.getElementUtils().getTypeElement(Supplier.class.getCanonicalName()).asType());
	}

	@Override
	public WirableSocketBeanInfo createModuleSocket(Element element) {
		if(!TypeElement.class.isAssignableFrom(element.getClass())) {
			throw new IllegalArgumentException("Element must be a TypeElement");
		}
		
		TypeElement typeElement = (TypeElement)element;
		
		Optional<? extends AnnotationMirror> annotation = this.processingEnvironment.getElementUtils().getAllAnnotationMirrors(element).stream().filter(a -> this.processingEnvironment.getTypeUtils().isSameType(a.getAnnotationType(), this.beanAnnotationType)).findFirst();
		if(!annotation.isPresent()) {
			throw new IllegalArgumentException("The specified element is not annotated with " + Bean.class.getSimpleName());
		}
		if(!typeElement.getEnclosingElement().getEnclosingElement().equals(this.moduleElement)) {
			throw new IllegalArgumentException("The specified element doesn't belong to module " + this.moduleQName);
		}
		
		ReporterInfo beanReporter = this.getReporter(element, annotation.get());
		
		if(!element.getKind().equals(ElementKind.INTERFACE)) {
			beanReporter.error("A socket bean element must be an interface");
			return null;
		}
		
		Optional<? extends TypeMirror> supplierType = typeElement.getInterfaces().stream().filter(t -> this.processingEnvironment.getTypeUtils().isSameType(this.processingEnvironment.getTypeUtils().erasure(t), this.supplierType)).findFirst();
		if(!supplierType.isPresent()) {
			beanReporter.error("A socket bean element must extend " + Supplier.class.getCanonicalName());
			return null;
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
			beanReporter.error("A socket bean must always be public");
			return null;
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
			socketName = element.getSimpleName().toString();
			socketName = Character.toLowerCase(socketName.charAt(0)) + socketName.substring(1);
		}

		BeanQualifiedName socketQName = null;
		try {
			socketQName = new BeanQualifiedName(this.moduleQName, socketName);
		} catch (QualifiedNameFormatException e) {
			beanReporter.error("Invalid socket bean qualified name: " + e.getMessage());
			return null;
		}

		// Optional or non-optional will be resolved during wiring {@see io.winterframework.core.compiler.wire.SocketResolver}
		MultiSocketType multiType = this.getMultiType(beanType);
		if(multiType != null) {
			return new CommonMultiSocketBeanInfo(this.processingEnvironment, typeElement, annotation.get(), socketQName, beanType, typeElement.asType(), true, multiType);
		}
		else {
			return new CommonSingleSocketBeanInfo(this.processingEnvironment, typeElement, annotation.get(), socketQName, beanType, typeElement.asType(), true);
		}
	}

}
