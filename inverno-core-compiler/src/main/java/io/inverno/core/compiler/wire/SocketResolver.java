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
package io.inverno.core.compiler.wire;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.annotation.processing.ProcessingEnvironment;

import io.inverno.core.annotation.Wire;
import io.inverno.core.compiler.spi.BeanInfo;
import io.inverno.core.compiler.spi.BeanQualifiedName;
import io.inverno.core.compiler.spi.ModuleBeanSocketInfo;
import io.inverno.core.compiler.spi.ModuleQualifiedName;
import io.inverno.core.compiler.spi.MultiSocketInfo;
import io.inverno.core.compiler.spi.SingleSocketInfo;
import io.inverno.core.compiler.spi.SocketInfo;
import io.inverno.core.compiler.spi.WiringStrategy;

/**
 * <p>
 * Resolves single and multiple sockets in a list of beans. The socket resolver
 * is at the heart of Inverno dependency injection mechanism.
 * </p>
 * 
 * @author <a href="mailto:jeremy.kuhn@inverno.io">Jeremy Kuhn</a>
 *
 */
public class SocketResolver {

	private ProcessingEnvironment processingEnvironment;
	
	private ModuleQualifiedName moduleQName;

	private List<? extends BeanInfo> beans;
	
	private Map<BeanQualifiedName, List<BeanInfo>> beansByQName;
	
	private List<WiringStrategy> selectorWiringStrategies;
	
	public SocketResolver(ProcessingEnvironment processingEnvironment, ModuleQualifiedName moduleQName, List<? extends BeanInfo> beans) {
		this.processingEnvironment = processingEnvironment;
		this.moduleQName = moduleQName;
		this.beans = beans;
		this.beansByQName = beans.stream().collect(Collectors.groupingBy(bean -> bean.getQualifiedName()));
		
		this.selectorWiringStrategies = List.of(
			new TypeWiringStrategy(this.processingEnvironment, this.moduleQName), 
			new AnnotationSelectorWiringStrategy(this.processingEnvironment)
		);
	}
	
	private boolean isWirable(BeanInfo bean, SocketInfo socket) {
		return this.selectorWiringStrategies.stream().allMatch(strategy -> strategy.isWirable(bean, socket));
	}
	
	private BeanInfo[] resolveByExplicitWiring(SocketInfo socket, WireInfo<?> wire) {
		List<BeanInfo> result = new ArrayList<>();
		for(BeanQualifiedName wiredBeanQName : wire.getBeans()) {
			if(this.beansByQName.containsKey(wiredBeanQName)) {
				List<BeanInfo> beans = this.beansByQName.get(wiredBeanQName);
				if(beans.size() > 1) {
					// Can't wire: multiple beans exist with name...
					wire.error("Can't wire different beans with same name " + wiredBeanQName + " into " + socket.getQualifiedName());
				}
				else {
					BeanInfo bean = beans.get(0);
					if(this.isWirable(bean, socket)) {
						// OK
						result.add(bean);
					}
					else {
						// Incompatible type
						wire.error("Bean " + bean.getQualifiedName() + " of type " + bean.getType() + " is not wirable into socket " + socket.getQualifiedName() + " of type " + socket.getType());
					}
				}
			}
			else {
				// Unknown source bean:
				// - when trying to explicitly wire a nested configuration bean into a compiled module socket
				wire.error("There's no bean named " + wiredBeanQName + " that can be wired to " + socket.getQualifiedName());
			}
		}
		return result.stream().toArray(BeanInfo[]::new);
	}
	
	public BeanInfo[] resolve(MultiSocketInfo socket, List<? extends WireInfo<?>> socketWires) {
		BeanInfo[] result = null;
		if(socketWires != null) {
			List<? extends WireInfo<?>> wires = socketWires.stream().filter(wire -> wire.getInto().equals(socket.getQualifiedName())).collect(Collectors.toList());
			if(wires.size() == 1) {
				WireInfo<?> wire = wires.get(0);
				BeanInfo[] explicitWiredBeans = this.resolveByExplicitWiring(socket, wire);
				if(explicitWiredBeans.length > 0) {
					result = explicitWiredBeans;
				}
				else {
					// No beans found due to a defective wire, errors already reported
					return null;
				}
			}
			else if(wires.size() > 1) {
				// Multiple wires defined for the socket
				for(WireInfo<?> wire : wires) {
					wire.error("Multiple wires targeting socket " + socket.getQualifiedName() + " were found");
				}
				return null;
			}
		}
		
		if(result == null) {
			// Autowiring for a multi socket
			List<BeanInfo> matchingBeans = beans.stream()
				.filter(beanInfo -> this.isWirable(beanInfo, socket))
				.collect(Collectors.toList());
			
			if(matchingBeans.size() == 0) {
				if(!socket.isOptional()) {
					socket.error("No bean was found matching required socket " + socket.getQualifiedName() + " of type " + socket.getType() + ", consider defining a bean or socket bean matching the socket in module " + this.moduleQName);
				}
				else {
					socket.warning("No bean was found matching optional socket " + socket.getQualifiedName() + " of type " + socket.getType());
				}
				return null;
			}
			else {
				result = matchingBeans.stream().toArray(BeanInfo[]::new);
			}
		}
		
		if(socket instanceof ModuleBeanSocketInfo) {
			// Filter out self
			result = Arrays.stream(result).filter(beanInfo -> !beanInfo.getQualifiedName().equals(((ModuleBeanSocketInfo)socket).getQualifiedName().getBeanQName())).toArray(BeanInfo[]::new);
		}
		return result;
	}
	
	public BeanInfo resolve(SingleSocketInfo socket, List<? extends WireInfo<?>> socketWires) {
		BeanInfo result = null;
		if(socketWires != null) {
			List<? extends WireInfo<?>> wires = socketWires.stream().filter(wire -> wire.getInto().equals(socket.getQualifiedName())).collect(Collectors.toList());
			if(wires.size() == 1) {
				WireInfo<?> wire = wires.get(0);
				if(wire.getBeans().length > 1) {
					// Invalid wire: multipe beans to be injected in a single socket
					wires.get(0).error("Can't wire multiple beans in single socket " + socket.getQualifiedName());
					return null;
				}
				else {
					BeanInfo[] explicitWiredBeans = this.resolveByExplicitWiring(socket, wire);
					
					if(explicitWiredBeans.length == 1) {
						// OK
						result = explicitWiredBeans[0];
					}
					else if(explicitWiredBeans.length > 1) {
						// This should never happen since we made sure only one bean is specified in the wire
						// We can't wire multiple beans in a single socket
						wire.error("Can't wire multiple beans in single socket " + socket.getQualifiedName());
						return null;
					}
					else {
						// No bean found due to a defective wire, errors already reported
						return null;
					}
				}
			}
			else if(wires.size() > 1) {
				// Multiple wires defined for the socket
				for(WireInfo<?> wire : wires) {
					wire.error("Multiple wires targeting socket " + socket.getQualifiedName() + " were found");
				}
				return null;
			}
		}
		
		if(result == null) {
			// Autowiring for a single socket
			List<BeanInfo> matchingBeans = beans.stream()
				.filter(beanInfo -> this.isWirable(beanInfo, socket) && (!(socket instanceof ModuleBeanSocketInfo) ||  !beanInfo.getQualifiedName().equals(((ModuleBeanSocketInfo)socket).getQualifiedName().getBeanQName())))
				.collect(Collectors.toList());
		
			if(matchingBeans.size() == 0) {
				if(!socket.isOptional()) {
					socket.error("No bean was found matching required socket " + socket.getQualifiedName() + " of type " + socket.getType() + ", consider defining a bean or a socket bean matching the socket in module " + this.moduleQName);
				}
				else {
					socket.warning("No bean was found matching optional socket " + socket.getQualifiedName() + " of type " + socket.getType());
				}
				return null;
			}
			else if(matchingBeans.size() > 1) {
				StringBuilder message = new StringBuilder();
				message.append("Multiple beans matching socket " + socket.getQualifiedName() + " were found\n");
				message.append(matchingBeans.stream().map(matchingBean -> "- " + matchingBean.getQualifiedName() + " of type " + matchingBean.getType() + "\n").collect(Collectors.joining()));
				message.append("\nConsider specifying an explicit wiring in module " + this.moduleQName + " (eg. @"+ Wire.class.getCanonicalName() + "(beans=\""+ matchingBeans.get(0).getQualifiedName() + "\", into=\"" + socket.getQualifiedName() +"\") )\n ");
				socket.error(message.toString());
				
				return null;
			}
			else {
				result = matchingBeans.get(0);
			}
		}
		
		return result;
	}
}
