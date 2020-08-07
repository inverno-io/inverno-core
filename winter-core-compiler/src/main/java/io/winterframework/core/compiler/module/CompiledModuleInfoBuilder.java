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
package io.winterframework.core.compiler.module;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.ModuleElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;

import io.winterframework.core.annotation.Wire;
import io.winterframework.core.annotation.Wires;
import io.winterframework.core.compiler.common.MutableMultiSocketInfo;
import io.winterframework.core.compiler.common.MutableSingleSocketInfo;
import io.winterframework.core.compiler.common.MutableSocketBeanInfo;
import io.winterframework.core.compiler.cycle.BeanCycleDetector;
import io.winterframework.core.compiler.cycle.BeanCycleDetector.CycleInfo;
import io.winterframework.core.compiler.spi.BeanInfo;
import io.winterframework.core.compiler.spi.ConfigurationInfo;
import io.winterframework.core.compiler.spi.ModuleBeanInfo;
import io.winterframework.core.compiler.spi.ModuleBeanSocketInfo;
import io.winterframework.core.compiler.spi.ModuleInfo;
import io.winterframework.core.compiler.spi.ModuleInfoBuilder;
import io.winterframework.core.compiler.spi.ModuleQualifiedName;
import io.winterframework.core.compiler.spi.MultiSocketInfo;
import io.winterframework.core.compiler.spi.QualifiedName;
import io.winterframework.core.compiler.spi.SingleSocketInfo;
import io.winterframework.core.compiler.spi.SocketBeanInfo;
import io.winterframework.core.compiler.wire.SocketResolver;
import io.winterframework.core.compiler.wire.WireInfo;
import io.winterframework.core.compiler.wire.WireInfoFactory;

/**
 * <p>
 * A module info builder used to build compiled module info from module elements
 * annotated with {@link Module} and currently compiled by the Java compiler.
 * </p>
 * 
 * @author jkuhn
 *
 */
class CompiledModuleInfoBuilder extends AbstractModuleInfoBuilder {

	private ModuleBeanInfo[] beans;
	
	private SocketBeanInfo[] sockets;
	
	private ConfigurationInfo[] configurations;
	
	private ModuleInfo[] modules;
	
	private ModuleSocketWiredBeansResolver moduleSocketWiredBeansResolver;
	
	public CompiledModuleInfoBuilder(ProcessingEnvironment processingEnvironment, ModuleElement moduleElement) {
		super(processingEnvironment, moduleElement);
		
		this.moduleSocketWiredBeansResolver = new ModuleSocketWiredBeansResolver();
		this.beans = new ModuleBeanInfo[0];
		this.sockets = new SocketBeanInfo[0];
		this.configurations = new ConfigurationInfo[0];
		this.modules = new ModuleInfo[0];
	}
	
	@Override
	public ModuleQualifiedName getQualifiedName() {
		return this.moduleQName;
	}
	
	@Override
	public ModuleInfoBuilder beans(ModuleBeanInfo[] beans) {
		this.beans = beans != null ? beans : new ModuleBeanInfo[0];
		return this;
	}

	@Override
	public ModuleInfoBuilder sockets(SocketBeanInfo[] sockets) {
		this.sockets = sockets != null ? sockets : new SocketBeanInfo[0];
		Arrays.stream(this.sockets).forEach(socket -> ((MutableSocketBeanInfo)socket).setOptional(true));
		return this;
	}
	
	@Override
	public ModuleInfoBuilder configurations(ConfigurationInfo[] configurations) {
		this.configurations = configurations != null ? configurations : new ConfigurationInfo[0];
		return this;
	}

	@Override
	public ModuleInfoBuilder modules(ModuleInfo[] modules) {
		this.modules = modules != null ? modules : new ModuleInfo[0];
		return this;
	}

	@Override
	public ModuleInfo build() {
		boolean hasNameConflicts = this.checkNameConflicts();
		boolean socketsResolved = this.resolveSockets();
		boolean hasBeanCycles = this.checkBeanCycles();
		
		CompiledModuleInfo moduleInfo = new CompiledModuleInfo(this.processingEnvironment, this.moduleElement, this.moduleAnnotation, this.moduleQName, this.version, Arrays.asList(this.beans), Arrays.asList(this.sockets), Arrays.asList(this.configurations), Arrays.asList(this.modules));
		moduleInfo.setFaulty(hasNameConflicts || hasBeanCycles || !socketsResolved);
		if(!hasBeanCycles) {
			moduleInfo.accept(this.moduleSocketWiredBeansResolver, null);
		}
		
		return moduleInfo;
	}
	
	private boolean checkNameConflicts() {
		// Verify beans with identical name => report a compilation error on both beans at @Bean annotation level
		Map<String, List<BeanInfo>> beansByName = Stream.concat(Arrays.stream(this.beans), Arrays.stream(this.sockets)).collect(Collectors.groupingBy(bean -> bean.getQualifiedName().getBeanName()));
		List<String> moduleNames = Arrays.stream(this.modules).map(moduleInfo -> moduleInfo.getQualifiedName().getValue()).collect(Collectors.toList());
		
		boolean hasConflicts = false;
		
		for(Entry<String, List<BeanInfo>> e : beansByName.entrySet()) {
			if(e.getValue().size() > 1) {
				e.getValue().stream().forEach(beanInfo -> beanInfo.error("Multiple beans with name " + e.getKey() + " exist in module " + this.moduleQName));
				hasConflicts = true; 
			}
			if(moduleNames.contains(e.getKey())) {
				// If a bean has the same name as a required module, we can have conflict when explicitly wire beans into required module socket (beanName:socketName vs moduleName:socketName)
				e.getValue().stream().forEach(beanInfo -> beanInfo.error("Bean is conflicting with module: " + e.getKey()));
				hasConflicts = true;
			}
		}
		return hasConflicts;
	}
	
	@SuppressWarnings("unchecked")
	private List<WireInfo<?>> extractWireInfos() {
		List<WireInfo<?>> result = new ArrayList<>();
		
		List<BeanInfo> wirableBeans = new ArrayList<>();
		// compiled module beans + sockets + public beans in component modules
		wirableBeans.addAll(Arrays.stream(this.beans).collect(Collectors.toList()));
		wirableBeans.addAll(Arrays.stream(this.sockets).collect(Collectors.toList()));
		wirableBeans.addAll(Arrays.stream(this.configurations).map(ConfigurationInfo::getSocket).collect(Collectors.toList()));
		// TODO These can actually only be inserted in component module
		wirableBeans.addAll(Arrays.stream(this.configurations).flatMap(configurationInfo -> Arrays.stream(configurationInfo.getNestedConfigurationProperties())).collect(Collectors.toList())); 
		wirableBeans.addAll(Arrays.stream(this.modules).flatMap(moduleInfo -> Arrays.stream(moduleInfo.getPublicBeans())).collect(Collectors.toList()));
		
		// Ignore beans with conflicting name
		wirableBeans = wirableBeans.stream().collect(Collectors.groupingBy(BeanInfo::getQualifiedName)).entrySet().stream().filter(e -> e.getValue().size() == 1).map(e -> e.getValue().get(0)).collect(Collectors.toList());
		
		List<ModuleBeanSocketInfo> beanSockets = Arrays.stream(this.beans).flatMap(beanInfo -> Arrays.stream(beanInfo.getSockets())).collect(Collectors.toList());
		
		List<SocketBeanInfo> requiredModuleSockets = Arrays.stream(this.modules).flatMap(moduleInfo -> Arrays.stream(moduleInfo.getSockets())).collect(Collectors.toList());
		
		WireInfoFactory wireInfoFactory = WireInfoFactory.create(this.processingEnvironment, this.moduleElement, wirableBeans, beanSockets, requiredModuleSockets);
		
		TypeMirror wireAnnotationType = this.processingEnvironment.getElementUtils().getTypeElement(Wire.class.getCanonicalName()).asType();
		TypeMirror wiresAnnotationType = this.processingEnvironment.getElementUtils().getTypeElement(Wires.class.getCanonicalName()).asType();
		
		Map<DeclaredType, List<AnnotationMirror>> annotationsByType = this.moduleElement.getAnnotationMirrors().stream().collect(Collectors.groupingBy(a -> a.getAnnotationType()));
		annotationsByType.entrySet().stream().forEach(e -> {
			e.getValue().stream().forEach(annotation -> {
				if(this.processingEnvironment.getTypeUtils().isSameType(e.getKey(), wireAnnotationType)) {
					WireInfo<?> wire = wireInfoFactory.createWire(annotation);
					if(wire != null) {
						result.add(wire);
					}
				}
				else if(this.processingEnvironment.getTypeUtils().isSameType(e.getKey(), wiresAnnotationType)) {
					((Collection<? extends AnnotationValue>)annotation.getElementValues().values().iterator().next().getValue()).stream()
						.forEach(value -> {
							WireInfo<?> wire = wireInfoFactory.createWire((AnnotationMirror)value.getValue());
							if(wire != null) {
								result.add(wire);
							}
						});
				}
			});
		});
		return result;
	}
	
	private boolean resolveSockets() {
		boolean resolved = true;
		
		List<WireInfo<?>> wireInfos = this.extractWireInfos();
		
		List<BeanInfo> resolverBeans = new ArrayList<>();
		resolverBeans.addAll(Arrays.asList(this.beans));
		resolverBeans.addAll(Arrays.asList(this.sockets));
		resolverBeans.addAll(Arrays.stream(this.configurations).map(ConfigurationInfo::getSocket).collect(Collectors.toList()));
		resolverBeans.addAll(Arrays.stream(this.modules)
			.flatMap(moduleInfo -> Arrays.stream(moduleInfo.getBeans()))
			.collect(Collectors.toList())
		);
		
		SocketResolver socketResolver = new SocketResolver(this.processingEnvironment, this.moduleQName, resolverBeans);

		Map<QualifiedName, List<WireInfo<?>>> wiresByBeanQName = wireInfos.stream().collect(Collectors.groupingBy(wire -> wire.getInto()));
		
		for(ModuleBeanInfo beanInfo : this.beans) {
			for(ModuleBeanSocketInfo socket : beanInfo.getSockets()) {
				if(MultiSocketInfo.class.isAssignableFrom(socket.getClass())) {
					BeanInfo[] resolvedBeans = socketResolver.resolve((MultiSocketInfo)socket, wiresByBeanQName.get(socket.getQualifiedName()));
					((MutableMultiSocketInfo)socket).setBeans(resolvedBeans);
					
					if(resolvedBeans != null && !socket.isOptional()) {
						Arrays.stream(resolvedBeans)
							.filter(resolvedBeanInfo -> SocketBeanInfo.class.isAssignableFrom(resolvedBeanInfo.getClass()))
							.forEach(resolvedSocket -> ((MutableSocketBeanInfo)resolvedSocket).setOptional(false));
					}
				}
				else if(SingleSocketInfo.class.isAssignableFrom(socket.getClass())) {
					BeanInfo resolvedBean = socketResolver.resolve((SingleSocketInfo)socket, wiresByBeanQName.get(socket.getQualifiedName()));
					((MutableSingleSocketInfo)socket).setBean(resolvedBean);
					
					if(resolvedBean != null && !socket.isOptional() && SocketBeanInfo.class.isAssignableFrom(resolvedBean.getClass())) {
						((MutableSocketBeanInfo)resolvedBean).setOptional(false);
					}
				}
				else {
					// This should never happen
					socket.error("Unable to resolve socket");
				}
				resolved = resolved ? socket.isOptional() || socket.isResolved() : false;
			}
		}
		
		for(ModuleInfo moduleInfo : this.modules) {
			resolverBeans.clear();
			resolverBeans.addAll(Arrays.asList(this.beans));
			resolverBeans.addAll(Arrays.asList(this.sockets));
			resolverBeans.addAll(Arrays.stream(this.configurations).flatMap(configurationInfo -> Arrays.stream(configurationInfo.getNestedConfigurationProperties())).collect(Collectors.toList()));
			resolverBeans.addAll(Arrays.stream(this.modules)
				.filter(moduleInfo2  -> !moduleInfo2.equals(moduleInfo))
				.flatMap(moduleInfo2 -> Arrays.stream(moduleInfo2.getBeans()))
				.collect(Collectors.toList())
			);
			
			socketResolver = new SocketResolver(this.processingEnvironment, this.moduleQName, resolverBeans);
			
			for(SocketBeanInfo socket : Stream.concat(Arrays.stream(moduleInfo.getSockets()), Arrays.stream(moduleInfo.getConfigurations()).map(ConfigurationInfo::getSocket)).collect(Collectors.toList())) {	
				if(MultiSocketInfo.class.isAssignableFrom(socket.getClass())) {
					BeanInfo[] resolvedBeans = socketResolver.resolve((MultiSocketInfo)socket, wiresByBeanQName.get(socket.getQualifiedName()));
					((MutableMultiSocketInfo)socket).setBeans(resolvedBeans);
					
					if(resolvedBeans != null && !socket.isOptional()) {
						Arrays.stream(resolvedBeans)
							.filter(resolvedBeanInfo -> SocketBeanInfo.class.isAssignableFrom(resolvedBeanInfo.getClass()))
							.forEach(resolvedSocket -> ((MutableSocketBeanInfo)resolvedSocket).setOptional(false));
					}
				}
				else if(SingleSocketInfo.class.isAssignableFrom(socket.getClass())) {
					BeanInfo resolvedBean = socketResolver.resolve((SingleSocketInfo)socket, wiresByBeanQName.get(socket.getQualifiedName()));
					((MutableSingleSocketInfo)socket).setBean(resolvedBean);
					
					if(resolvedBean != null && !socket.isOptional() && SocketBeanInfo.class.isAssignableFrom(resolvedBean.getClass())) {
						((MutableSocketBeanInfo)resolvedBean).setOptional(false);
					}
				}
				resolved = resolved ? socket.isOptional() || socket.isResolved() : false;	
			}
		}
		return resolved;
	}
	
	private boolean checkBeanCycles() {
		BeanCycleDetector detector = new BeanCycleDetector(this.moduleQName, Stream.concat(Arrays.stream(this.beans), Arrays.stream(this.sockets)).collect(Collectors.toList()));
		List<List<CycleInfo>> beanCycles = detector.findCycles();
		
		for(List<CycleInfo> cycle : beanCycles) {
			StringBuilder messageBuilder = new StringBuilder();
			//message.append("The following beans form a cycle in module " + this.moduleQName + "\n");

			int maxBeanQNameLength = cycle.stream().filter(cycleInfo -> !SocketBeanInfo.class.isAssignableFrom(cycleInfo.getSocketInfo().getClass())).mapToInt(cycleInfo -> cycleInfo.getBeanInfo().getQualifiedName().getValue().length()).max().getAsInt();
			char[] maxPad = new char[Math.floorDiv(maxBeanQNameLength, 2) + 4];
			Arrays.fill(maxPad, '\u2500');
			messageBuilder.append('\u250C').append(maxPad).append("\u2510\n");
			
			Arrays.fill(maxPad, ' ');
			messageBuilder.append('\u2502').append(maxPad).append("\u2502\n");
			
			boolean isWiredTo = false;
			for(int i = 0;i<cycle.size();i++) {
				CycleInfo cycleInfo = cycle.get(i);
				boolean isSocketBean = SocketBeanInfo.class.isAssignableFrom(cycleInfo.getSocketInfo().getClass());
				boolean isOpaqueSocket = isSocketBean && isWiredTo;
				isWiredTo = ModuleBeanSocketInfo.class.isAssignableFrom(cycleInfo.getSocketInfo().getClass()) ? !((ModuleBeanSocketInfo)cycleInfo.getSocketInfo()).getQualifiedName().getBeanQName().equals(cycleInfo.getBeanInfo().getQualifiedName()) : false;
				
				String beanName = cycleInfo.getBeanInfo().getQualifiedName().getValue();
				
				Arrays.fill(maxPad, ' ');
				String linkLine = String.valueOf(maxPad) + (isSocketBean ? '\u250A' : '\u2502');
				String dependencyLine = (isOpaqueSocket ? String.valueOf(maxPad).substring(0, maxPad.length - 1) + "(\u2504)" : linkLine) + " " + cycleInfo.getSocketInfo().getQualifiedName().getValue();

				Arrays.fill(maxPad, ' ');
				String arrowLine = String.valueOf(maxPad) + "\u25BC";
				
				char[] pad = new char[maxPad.length - Math.floorDiv(beanName.length(), 2)];
				Arrays.fill(pad, ' ');
				
				// TODO we can't rely on the size anymore, the best would be to insert the \u25B2 afterwards 
				
				if(!isSocketBean) {
					messageBuilder.append("\u2502").append(pad).append(beanName).append("\n");
				}
				if(!isWiredTo) {
					messageBuilder.append("\u2502").append(linkLine).append("\n");
					messageBuilder.append("\u2502").append(dependencyLine).append("\n");
					messageBuilder.append("\u2502").append(linkLine).append("\n");
					
					if(i < cycle.size() - 1) {
						messageBuilder.append("\u2502").append(arrowLine).append("\n");
					}
				}
			}
			
			Arrays.fill(maxPad, '\u2500');
			messageBuilder.append('\u2514').append(maxPad).append("\u2518 \n");

			String[] messageLines = messageBuilder.toString().split("\n");
			messageLines[Math.floorDiv(messageLines.length, 2)] = "\u25B2" + messageLines[Math.floorDiv(messageLines.length, 2)].substring(1);

			String message = String.join("\n", messageLines);
			
			cycle.stream()
				.filter(cycleInfo -> cycleInfo.getBeanInfo().getQualifiedName().getModuleQName().equals(this.moduleQName))
				.forEach(cycleInfo -> cycleInfo.getBeanInfo().error("Bean " + cycleInfo.getBeanInfo().getQualifiedName() + " forms a cycle in module " + this.moduleQName + "\n" + message));
		}
		
		return beanCycles.size() > 0;
	}
}
