/**
 * 
 */
package io.winterframework.core.compiler.wire;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.type.ArrayType;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.type.WildcardType;

import io.winterframework.core.annotation.Wire;
import io.winterframework.core.compiler.spi.BeanInfo;
import io.winterframework.core.compiler.spi.BeanQualifiedName;
import io.winterframework.core.compiler.spi.ModuleBeanInfo;
import io.winterframework.core.compiler.spi.ModuleBeanSocketInfo;
import io.winterframework.core.compiler.spi.ModuleQualifiedName;
import io.winterframework.core.compiler.spi.MultiSocketInfo;
import io.winterframework.core.compiler.spi.SingleSocketInfo;
import io.winterframework.core.compiler.spi.SocketInfo;

/**
 * @author jkuhn
 *
 */
public class SocketResolver {

	private ProcessingEnvironment processingEnvironment;
	
	private ModuleQualifiedName moduleQName;

	private List<? extends BeanInfo> beans;
	
	private Map<BeanQualifiedName, List<BeanInfo>> beansByQName;
	
	public SocketResolver(ProcessingEnvironment processingEnvironment, ModuleQualifiedName moduleQName, List<? extends BeanInfo> beans) {
		this.processingEnvironment = processingEnvironment;
		this.moduleQName = moduleQName;
		this.beans = beans;
		this.beansByQName = beans.stream().collect(Collectors.groupingBy(bean -> bean.getQualifiedName()));
	}
	
	private BeanInfo[] resolveByExplicitWiring(SocketInfo socket, WireInfo<?> wire) {
		List<BeanInfo> result = new ArrayList<>();
		for(BeanQualifiedName wiredBeanQName : wire.getBeans()) {
			if(this.beansByQName.containsKey(wiredBeanQName)) {
				List<BeanInfo> beans = this.beansByQName.get(wiredBeanQName);
				if(beans.size() > 1) {
					// Can't wire: multiple beans exist with name...
					wire.error("Can't wire multiple beans with name " + wiredBeanQName + " into " + socket.getQualifiedName());
				}
				else {
					BeanInfo bean = beans.get(0);
					if(this.isAssignable(bean, socket)) {
						// OK
						result.add(bean);
					}
					else {
						// Incompatible type
						wire.error("Can't wire bean " + bean.getQualifiedName() + " of type " + bean.getType() + " which is not assignable to type " + socket.getType() + " of socket " + socket.getQualifiedName());
					}
				}
			}
			else {
				// Unknown source bean
				wire.error("There's no bean named " + wiredBeanQName);
			}
		}
		return result.stream().toArray(BeanInfo[]::new);
	}
	
	private boolean isAssignable(BeanInfo bean, SocketInfo socket) {
		if(ModuleBeanInfo.class.isAssignableFrom(bean.getClass()) && !bean.getQualifiedName().getModuleQName().equals(this.moduleQName) && ((ModuleBeanInfo)bean).getProvidedType() != null) {
			if(this.isAssignable(((ModuleBeanInfo)bean).getProvidedType(), socket)) {
				return true;
			}
			return false;
		}
		else {
			return this.isAssignable(bean.getType(), socket);
		}
	}

	private boolean isAssignable(TypeMirror type, SocketInfo socket) {
		if(MultiSocketInfo.class.isAssignableFrom(socket.getClass())) {
			DeclaredType socketCollectionType = this.processingEnvironment.getTypeUtils().getDeclaredType(this.processingEnvironment.getElementUtils().getTypeElement(Collection.class.getCanonicalName()), socket.getType());
			ArrayType socketArrayType;
			if(!socket.getType().getKind().equals(TypeKind.WILDCARD)) {
				socketArrayType = this.processingEnvironment.getTypeUtils().getArrayType(socket.getType());
			}
			else {
				socketArrayType = this.processingEnvironment.getTypeUtils().getArrayType(((WildcardType)socket.getType()).getExtendsBound());
			}
			return this.processingEnvironment.getTypeUtils().isAssignable(type, socket.getType()) || this.processingEnvironment.getTypeUtils().isAssignable(type, socketArrayType) || this.processingEnvironment.getTypeUtils().isAssignable(type, socketCollectionType);
		}
		else {
			// Single socket
			return this.processingEnvironment.getTypeUtils().isAssignable(type, socket.getType());
		}
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
				.filter(beanInfo -> this.isAssignable(beanInfo, socket))
				.collect(Collectors.toList());
			
			if(matchingBeans.size() == 0) {
				if(!socket.isOptional()) {
					socket.error("No bean was found matching required socket " + socket.getQualifiedName() + " of type " + socket.getType() + ", consider defining a bean or socket bean matching the socket in module " + this.moduleQName);
				}
				else {
					socket.warning("No bean was found matching optional socket " + socket.getQualifiedName() + " of type " + socket.getType() + ", socket will be empty");
				}
				return null;
			}
			else {
				result = matchingBeans.stream().toArray(BeanInfo[]::new);
			}
		}
		/*if(socket.isOptional() || Arrays.stream(result).anyMatch(beanInfo -> !(ModuleSocketInfo.class.isAssignableFrom(beanInfo.getClass()) || !((ModuleSocketInfo)beanInfo).isOptional()))) {
			return result;
		}
		else {
			String wiredBeans = Arrays.stream(result).map(bean -> bean.getQualifiedName().toString()).collect(Collectors.joining(", "));
			socket.error("All beans wired to non-optional multi socket " + socket.getQualifiedName() + " can't be all optional: " + wiredBeans);
			return null;
		}*/
		
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
					wires.get(0).error("Can't wire multiple beans into single socket " + socket.getQualifiedName() + ": " + Arrays.stream(wire.getBeans()).map(bean -> bean.getValue()).collect(Collectors.joining(", ")));
					return null;
				}
				else {
					BeanInfo[] explicitWiredBeans = this.resolveByExplicitWiring(socket, wire);
					
					if(explicitWiredBeans.length == 1) {
						// OK
						result = explicitWiredBeans[0];
					}
					else if(explicitWiredBeans.length > 1) {
						// We can't wire multiple beans in a single socket
						wire.error("Can't wire multiple beans in single socket " + socket.getQualifiedName());
						return null;
					}
					else {
						// No bean found due to a defective wire, errors already reported; 
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
				.filter(beanInfo -> this.isAssignable(beanInfo, socket) && (!(socket instanceof ModuleBeanSocketInfo) ||  !beanInfo.getQualifiedName().equals(((ModuleBeanSocketInfo)socket).getQualifiedName().getBeanQName())))
				.collect(Collectors.toList());
		
			if(matchingBeans.size() == 0) {
				if(!socket.isOptional()) {
					socket.error("No bean was found matching required socket " + socket.getQualifiedName() + " of type " + socket.getType() + ", consider defining a bean or a socket bean matching the socket in module " + this.moduleQName);
				}
				else {
					socket.warning("No bean was found matching optional socket " + socket.getQualifiedName() + " of type " + socket.getType() + ", socket will be null");
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
		
		/*if(!socket.isOptional() && ModuleSocketInfo.class.isAssignableFrom(result.getClass()) && ((ModuleSocketInfo)result).isOptional()) {
			// We are trying to wire an optional plug to a non optional socket
			socket.error("Bean " + result.getQualifiedName() + " wired to non-optional single socket " + socket.getQualifiedName() + " can't be optional");
			return null;
		}*/
		
		
		
		return result;
	}
}
