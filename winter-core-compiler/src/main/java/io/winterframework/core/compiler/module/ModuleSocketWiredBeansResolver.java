/**
 * 
 */
package io.winterframework.core.compiler.module;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import io.winterframework.core.compiler.socket.WirableSocketBeanInfo;
import io.winterframework.core.compiler.spi.BeanInfo;
import io.winterframework.core.compiler.spi.BeanQualifiedName;
import io.winterframework.core.compiler.spi.ModuleBeanInfo;
import io.winterframework.core.compiler.spi.ModuleBeanMultiSocketInfo;
import io.winterframework.core.compiler.spi.ModuleBeanSingleSocketInfo;
import io.winterframework.core.compiler.spi.ModuleBeanSocketInfo;
import io.winterframework.core.compiler.spi.ModuleInfo;
import io.winterframework.core.compiler.spi.ModuleInfoVisitor;
import io.winterframework.core.compiler.spi.MultiSocketBeanInfo;
import io.winterframework.core.compiler.spi.MultiSocketInfo;
import io.winterframework.core.compiler.spi.SingleSocketBeanInfo;
import io.winterframework.core.compiler.spi.SingleSocketInfo;
import io.winterframework.core.compiler.spi.SocketBeanInfo;
import io.winterframework.core.compiler.spi.SocketInfo;
import io.winterframework.core.compiler.spi.WrapperBeanInfo;

/**
 * @author jkuhn
 *
 */
class ModuleSocketWiredBeansResolver implements ModuleInfoVisitor<Void, Set<BeanQualifiedName>> {

	/* (non-Javadoc)
	 * @see io.winterframework.core.compiler.spi.ModuleInfoVisitor#visit(io.winterframework.core.compiler.spi.ModuleInfo, java.lang.Object)
	 */
	@Override
	public Void visit(ModuleInfo moduleInfo, Set<BeanQualifiedName> wiredBeans) {
		Arrays.stream(moduleInfo.getBeans()).forEach(bean -> {
			this.visit(bean, new HashSet<>());
		});
		
		return null;
	}

	/* (non-Javadoc)
	 * @see io.winterframework.core.compiler.spi.ModuleInfoVisitor#visit(io.winterframework.core.compiler.spi.BeanInfo, java.lang.Object)
	 */
	@Override
	public Void visit(BeanInfo beanInfo, Set<BeanQualifiedName> wiredBeans) {
		// Optional unresolved sockets
		if(beanInfo == null) {
			return null;
		}
		if(ModuleBeanInfo.class.isAssignableFrom(beanInfo.getClass())) {
			return this.visit((ModuleBeanInfo)beanInfo, wiredBeans);
		}
		else if(SocketBeanInfo.class.isAssignableFrom(beanInfo.getClass())) {
			return this.visit((SocketBeanInfo)beanInfo, wiredBeans);
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see io.winterframework.core.compiler.spi.ModuleInfoVisitor#visit(io.winterframework.core.compiler.spi.ModuleBeanInfo, java.lang.Object)
	 */
	@Override
	public Void visit(ModuleBeanInfo moduleBeanInfo, Set<BeanQualifiedName> wiredBeans) {
		wiredBeans.add(moduleBeanInfo.getQualifiedName());
		Arrays.stream(moduleBeanInfo.getSockets()).forEach(socket -> this.visit(socket, wiredBeans));
		
		return null;
	}

	/* (non-Javadoc)
	 * @see io.winterframework.core.compiler.spi.ModuleInfoVisitor#visit(io.winterframework.core.compiler.spi.ModuleWrapperBeanInfo, java.lang.Object)
	 */
	@Override
	public Void visit(WrapperBeanInfo moduleWrapperBeanInfo, Set<BeanQualifiedName> wiredBeans) {
		return this.visit((ModuleBeanInfo)moduleWrapperBeanInfo, wiredBeans);
	}
	
	/* (non-Javadoc)
	 * @see io.winterframework.core.compiler.spi.ModuleInfoVisitor#visit(io.winterframework.core.compiler.spi.ModuleBeanSocketInfo, java.lang.Object)
	 */
	@Override
	public Void visit(ModuleBeanSocketInfo beanSocketInfo, Set<BeanQualifiedName> wiredBeans) {
		if(ModuleBeanSingleSocketInfo.class.isAssignableFrom(beanSocketInfo.getClass())) {
			return this.visit((ModuleBeanSingleSocketInfo)beanSocketInfo, wiredBeans);
		}
		else if(ModuleBeanMultiSocketInfo.class.isAssignableFrom(beanSocketInfo.getClass())) {
			return this.visit((ModuleBeanMultiSocketInfo)beanSocketInfo, wiredBeans);
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see io.winterframework.core.compiler.spi.ModuleInfoVisitor#visit(io.winterframework.core.compiler.spi.ModuleBeanSingleSocketInfo, java.lang.Object)
	 */
	@Override
	public Void visit(ModuleBeanSingleSocketInfo beanSingleSocketInfo, Set<BeanQualifiedName> wiredBeans) {
		if(beanSingleSocketInfo.getBean() != null) {
			this.visit(beanSingleSocketInfo.getBean(), wiredBeans);
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see io.winterframework.core.compiler.spi.ModuleInfoVisitor#visit(io.winterframework.core.compiler.spi.ModuleBeanMultiSocketInfo, java.lang.Object)
	 */
	@Override
	public Void visit(ModuleBeanMultiSocketInfo beanMultiSocketInfo, Set<BeanQualifiedName> wiredBeans) {
		if(beanMultiSocketInfo.getBeans() != null) {
			Arrays.stream(beanMultiSocketInfo.getBeans()).forEach(bean -> this.visit(bean, wiredBeans));
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see io.winterframework.core.compiler.spi.ModuleInfoVisitor#visit(io.winterframework.core.compiler.spi.ModuleSocketInfo, java.lang.Object)
	 */
	@Override
	public Void visit(SocketBeanInfo moduleSocketInfo, Set<BeanQualifiedName> wiredBeans) {
		wiredBeans.addAll(Arrays.asList(moduleSocketInfo.getWiredBeans()));
		((WirableSocketBeanInfo)moduleSocketInfo).setWiredBeans(wiredBeans);
		return null;
	}

	/* (non-Javadoc)
	 * @see io.winterframework.core.compiler.spi.ModuleInfoVisitor#visit(io.winterframework.core.compiler.spi.ModuleSingleSocketInfo, java.lang.Object)
	 */
	@Override
	public Void visit(SingleSocketBeanInfo moduleSingleSocketInfo, Set<BeanQualifiedName> wiredBeans) {
		return null;
	}

	/* (non-Javadoc)
	 * @see io.winterframework.core.compiler.spi.ModuleInfoVisitor#visit(io.winterframework.core.compiler.spi.ModuleMultiSocketInfo, java.lang.Object)
	 */
	@Override
	public Void visit(MultiSocketBeanInfo moduleMultiSocketInfo, Set<BeanQualifiedName> wiredBeans) {
		return null;
	}

	/* (non-Javadoc)
	 * @see io.winterframework.core.compiler.spi.ModuleInfoVisitor#visit(io.winterframework.core.compiler.spi.SocketInfo, java.lang.Object)
	 */
	@Override
	public Void visit(SocketInfo socketInfo, Set<BeanQualifiedName> wiredBeans) {
		return null;
	}

	/* (non-Javadoc)
	 * @see io.winterframework.core.compiler.spi.ModuleInfoVisitor#visit(io.winterframework.core.compiler.spi.SingleSocketInfo, java.lang.Object)
	 */
	@Override
	public Void visit(SingleSocketInfo singleSocketInfo, Set<BeanQualifiedName> wiredBeans) {
		return null;
	}

	/* (non-Javadoc)
	 * @see io.winterframework.core.compiler.spi.ModuleInfoVisitor#visit(io.winterframework.core.compiler.spi.MultiSocketInfo, java.lang.Object)
	 */
	@Override
	public Void visit(MultiSocketInfo multiSocketInfo, Set<BeanQualifiedName> wiredBeans) {
		return null;
	}

}
