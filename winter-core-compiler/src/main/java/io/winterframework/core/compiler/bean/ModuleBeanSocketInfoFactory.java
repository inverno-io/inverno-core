/**
 * 
 */
package io.winterframework.core.compiler.bean;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.ModuleElement;
import javax.lang.model.element.VariableElement;

import io.winterframework.core.compiler.common.AbstractSocketInfoFactory;
import io.winterframework.core.compiler.spi.BeanQualifiedName;
import io.winterframework.core.compiler.spi.BeanSocketQualifiedName;
import io.winterframework.core.compiler.spi.ModuleBeanSocketInfo;
import io.winterframework.core.compiler.spi.SocketBeanInfo;
import io.winterframework.core.compiler.spi.MultiSocketType;

/**
 * @author jkuhn
 *
 */
class ModuleBeanSocketInfoFactory extends AbstractSocketInfoFactory {

	private BeanQualifiedName beanQName;
	
	/**
	 * 
	 */
	protected ModuleBeanSocketInfoFactory(ProcessingEnvironment processingEnvironment, ModuleElement moduleElement, BeanQualifiedName beanQName) {
		super(processingEnvironment, moduleElement);
		
		this.beanQName = beanQName;
	}

	public static ModuleBeanSocketInfoFactory create(ProcessingEnvironment processingEnvironment, ModuleElement moduleElement, BeanQualifiedName beanQName) {
		return new ModuleBeanSocketInfoFactory(processingEnvironment, moduleElement, beanQName);
	}
	
	// Compiled
	public ModuleBeanSocketInfo createBeanSocket(VariableElement variableElement) {
		if(!variableElement.getKind().equals(ElementKind.PARAMETER)) {
			throw new IllegalArgumentException("Element must be a parameter");
		}
		ExecutableElement socketElement = (ExecutableElement)variableElement.getEnclosingElement();
		String socketName = null;
		boolean optional = false;
		if(socketElement.getKind().equals(ElementKind.CONSTRUCTOR)) {
			socketName = variableElement.getSimpleName().toString();
		}
		else {
			if(!socketElement.getModifiers().contains(Modifier.PUBLIC) || !socketElement.getSimpleName().toString().startsWith("set") || socketElement.getParameters().size() != 1) {
				throw new IllegalArgumentException("Invalid setter method which should be a single-argument method");
			}
			
			socketName = socketElement.getSimpleName().toString().substring(3);
			socketName = Character.toLowerCase(socketName.charAt(0)) + socketName.substring(1);
			optional = true;
		}
		
		// This should never throw a QualifiedNameFormatException as a Java variable is a valid qualified name part
		BeanSocketQualifiedName socketQName = new BeanSocketQualifiedName(this.beanQName, socketName);
		
		MultiSocketType multiType = this.getMultiType(variableElement.asType());
		if(multiType != null) {
			return new CommonModuleBeanMultiSocketInfo(this.processingEnvironment, variableElement, socketQName, this.getComponentType(variableElement.asType()), socketElement, optional, multiType);
		}
		else {
			return new CommonModuleBeanSingleSocketInfo(this.processingEnvironment, variableElement, socketQName, variableElement.asType(), socketElement, optional);
		}
	}
	
	// Imported
	public ModuleBeanSocketInfo createBeanSocket(SocketBeanInfo moduleSocketInfo) {
		BeanSocketQualifiedName socketQName = new BeanSocketQualifiedName(moduleSocketInfo.getQualifiedName(), moduleSocketInfo.getQualifiedName().getSimpleValue());
		return new CommonModuleBeanSingleSocketInfo(this.processingEnvironment, this.moduleElement, socketQName, moduleSocketInfo.getType(), null, moduleSocketInfo.isOptional());
	}
}
