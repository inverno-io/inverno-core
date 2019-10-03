/**
 * 
 */
package io.winterframework.core.compiler.wire;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.ModuleElement;
import javax.lang.model.type.TypeMirror;

import io.winterframework.core.annotation.Wire;
import io.winterframework.core.annotation.Wires;
import io.winterframework.core.compiler.common.AbstractInfoFactory;
import io.winterframework.core.compiler.common.ReporterInfo;
import io.winterframework.core.compiler.spi.BeanInfo;
import io.winterframework.core.compiler.spi.BeanQualifiedName;
import io.winterframework.core.compiler.spi.BeanSocketQualifiedName;
import io.winterframework.core.compiler.spi.ModuleBeanSocketInfo;
import io.winterframework.core.compiler.spi.QualifiedNameFormatException;
import io.winterframework.core.compiler.spi.SocketBeanInfo;

/**
 * @author jkuhn
 *
 */
public class WireInfoFactory extends AbstractInfoFactory {

	private Map<BeanQualifiedName, ? extends BeanInfo> beans;
	
	private Map<BeanSocketQualifiedName, ? extends ModuleBeanSocketInfo> beanSockets;
	
	private Map<BeanQualifiedName, ? extends SocketBeanInfo> importedModuleSockets;
	
	private TypeMirror wireAnnotationType;
	private TypeMirror wiresAnnotationType;
	
	protected WireInfoFactory(ProcessingEnvironment processingEnvironment, ModuleElement moduleElement, List<? extends BeanInfo> beans, List<? extends ModuleBeanSocketInfo> beanSockets, List<? extends SocketBeanInfo> importedModuleSockets) {
		super(processingEnvironment, moduleElement);
		this.processingEnvironment = processingEnvironment;
		this.moduleElement = moduleElement;
		
		this.beans = beans.stream().collect(Collectors.toMap(beanInfo -> beanInfo.getQualifiedName(), Function.identity()));
		this.beanSockets = beanSockets.stream().collect(Collectors.toMap(beanSocketInfo -> beanSocketInfo.getQualifiedName(), Function.identity()));
		this.importedModuleSockets = importedModuleSockets.stream().collect(Collectors.toMap(moduleSocketInfo -> moduleSocketInfo.getQualifiedName(), Function.identity()));
		
		this.wireAnnotationType = this.processingEnvironment.getElementUtils().getTypeElement(Wire.class.getCanonicalName()).asType();
		this.wiresAnnotationType = this.processingEnvironment.getElementUtils().getTypeElement(Wires.class.getCanonicalName()).asType();
	}
	
	public static WireInfoFactory create(ProcessingEnvironment processingEnvironment, ModuleElement moduleElement, List<? extends BeanInfo> beans, List<? extends ModuleBeanSocketInfo> beanSockets, List<? extends SocketBeanInfo> importedModuleSockets) {
		return new WireInfoFactory(processingEnvironment, moduleElement, beans,beanSockets, importedModuleSockets);
	}
	
	@SuppressWarnings("unchecked")
	public WireInfo<?> createWire(AnnotationMirror annotation) {
		if(!this.processingEnvironment.getTypeUtils().isSameType(annotation.getAnnotationType(), this.wireAnnotationType)) {
			throw new IllegalArgumentException("The specified annotation is not a @" + Wire.class.getSimpleName() + " annotation");
		}
		Optional<? extends AnnotationMirror> wireOnModule = this.moduleElement.getAnnotationMirrors().stream()
			.filter(a -> this.processingEnvironment.getTypeUtils().isSameType(a.getAnnotationType(), this.wireAnnotationType)).findFirst();
		
		if(wireOnModule.isPresent()) {
			if(!wireOnModule.get().equals(annotation)) {
				throw new IllegalArgumentException("The specified @" + Wire.class.getSimpleName() + " annotation hasn't been declared on the module to build");
			}
		}
		else {
			Optional<? extends AnnotationMirror> wiresOnModule = this.moduleElement.getAnnotationMirrors().stream()
				.filter(a -> this.processingEnvironment.getTypeUtils().isSameType(a.getAnnotationType(), this.wiresAnnotationType)).findFirst();
			
			if(!wiresOnModule.isPresent() || !((Collection<? extends AnnotationValue>)wiresOnModule.get().getElementValues().values().iterator().next().getValue()).stream().map(v -> (AnnotationMirror)v.getValue()).anyMatch(a -> a.equals(annotation))) {
				throw new IllegalArgumentException("The specified @" + Wire.class.getSimpleName() + " annotation hasn't been declared on the module to build");
			}
		}
		
		ReporterInfo wireReporter = this.getReporter(this.moduleElement, annotation);
		
		String[] beans = null;
		String into = null;
		for(Entry<? extends ExecutableElement, ? extends AnnotationValue> value : this.processingEnvironment.getElementUtils().getElementValuesWithDefaults(annotation).entrySet()) {
			switch(value.getKey().getSimpleName().toString()) {
				case "beans" : beans = ((List<AnnotationValue>)value.getValue().getValue()).stream().map(v -> v.getValue()).toArray(String[]::new);
					break;
				case "into" : into = (String)value.getValue().getValue();
					break;
			}
		}
		
		BeanQualifiedName[] beanQNames = null;
		try {
			beanQNames = Arrays.stream(beans)
				.map(bean -> {
					try {
						return BeanQualifiedName.valueOf(bean);
					}
					catch(QualifiedNameFormatException e) {
						return new BeanQualifiedName(this.moduleQName, bean);
					}
				})
				.toArray(BeanQualifiedName[]::new);
		}
		catch(QualifiedNameFormatException e) {
			wireReporter.error("Invalid bean qualified name: " + e.getMessage());
			return null;
		}
		
		String unresolvedBeans = Arrays.stream(beanQNames)
			.filter(beanQName -> !this.beans.containsKey(beanQName))
			.map(beanQName -> beanQName.toString())
			.collect(Collectors.joining(", "));
		
		if(unresolvedBeans != null && !unresolvedBeans.equals("")) {
			wireReporter.error("Unkown beans: " + unresolvedBeans);
		}
		
		BeanSocketQualifiedName beanSocketQName = null;
		BeanQualifiedName moduleSocketQName = null;
		try {
			try {
				try {
					// BeanSocket with explicit module: <module>:<bean>:<socket>
					beanSocketQName = BeanSocketQualifiedName.valueOf(into);
				}
				catch(QualifiedNameFormatException e) {
					// BeanSocket with implicit module: <bean>:<socket>
					beanSocketQName = BeanSocketQualifiedName.valueOf(this.moduleQName, into);
				}

				if(!this.beanSockets.containsKey(beanSocketQName)) {
					// There's no bean socket with that name so let's try to interpret this as a ModuleSocket: <module>:<socket>
					beanSocketQName = null;
					throw new QualifiedNameFormatException();
				}
			}
			catch(QualifiedNameFormatException e) {
				// ModuleSocket <module>:<socket>
				moduleSocketQName = BeanQualifiedName.valueOf(into);
				if(moduleSocketQName.getModuleQName().equals(this.moduleQName)) {
					wireReporter.error("You can't wire beans to a socket bean defined in this module");
					return null;
				}
			}
		}
		catch(QualifiedNameFormatException e) {
			wireReporter.error("Invalid socket qualified name: " + e.getMessage());
			return null;
		}
		
		if(beanSocketQName != null) {
			// Bean Socket
			// We already check this socket exist in the provided list
			return new ModuleBeanSocketWireInfo(this.processingEnvironment, this.moduleElement, annotation, beanQNames, beanSocketQName);
		}
		else {
			// Module Socket
			if(!this.importedModuleSockets.containsKey(moduleSocketQName)) {
				wireReporter.error("There's no socket bean named: " + moduleSocketQName);
				return null;
			}
			return new SocketBeanWireInfo(this.processingEnvironment, this.moduleElement, annotation, beanQNames, moduleSocketQName);
		}
	}
}
