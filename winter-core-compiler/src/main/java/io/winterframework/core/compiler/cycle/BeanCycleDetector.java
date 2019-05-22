/**
 * 
 */
package io.winterframework.core.compiler.cycle;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import io.winterframework.core.compiler.spi.BeanInfo;
import io.winterframework.core.compiler.spi.ModuleBeanInfo;
import io.winterframework.core.compiler.spi.SocketBeanInfo;
import io.winterframework.core.compiler.spi.MultiSocketInfo;
import io.winterframework.core.compiler.spi.SingleSocketInfo;
import io.winterframework.core.compiler.spi.SocketInfo;

/**
 * @author jkuhn
 *
 */
public class BeanCycleDetector {

	private List<BeanInfo> beans;
	
	public BeanCycleDetector(List<BeanInfo> beans) {
		this.beans = beans;
	}
	
	public List<List<CycleInfo>> findCycles() {		
		CycleDetectionContext context = new CycleDetectionContext();
		for(BeanInfo bean : this.beans) {
			this.visitBean(bean, context);
		}
		return context.getCycles();
	}
	
	private void visitBean(BeanInfo bean, CycleDetectionContext context) {
		if(bean != null && !context.isVisited(bean)) {
			if(context.isOnStack(bean)) {
				// cycle detected
				context.addCycle(bean);
			}
			else {
				context.pushBean(bean);
				if(ModuleBeanInfo.class.isAssignableFrom(bean.getClass())) {
					for(SocketInfo socket : ((ModuleBeanInfo)bean).getSockets()) {
						context.pushSocket(socket);
						if(SingleSocketInfo.class.isAssignableFrom(socket.getClass())) {
							this.visitBean(((SingleSocketInfo)socket).getBean(), context);
						}
						else if(MultiSocketInfo.class.isAssignableFrom(socket.getClass())) {
							if(((MultiSocketInfo)socket).getBeans() != null) {
								Arrays.stream(((MultiSocketInfo)socket).getBeans()).forEach(b -> this.visitBean(b, context));
							}
						}
						context.popSocket();
					}
				}
				else if(SocketBeanInfo.class.isAssignableFrom(bean.getClass())) {
					context.pushSocket((SocketBeanInfo)bean);
					if(SingleSocketInfo.class.isAssignableFrom(bean.getClass())) {
						this.visitBean(((SingleSocketInfo)bean).getBean(), context);
					}
					else if(MultiSocketInfo.class.isAssignableFrom(bean.getClass())) {
						if(((MultiSocketInfo)bean).getBeans() != null) {
							Arrays.stream(((MultiSocketInfo)bean).getBeans()).forEach(b -> this.visitBean(b, context));
						}
					}
					context.popSocket();
				}
				context.popBean();
				context.setVisited(bean);
			}
		}		
	}
	
	public class CycleInfo {
		private BeanInfo beanInfo;
		
		private SocketInfo socketInfo;

		private CycleInfo(BeanInfo beanInfo, SocketInfo socketInfo) {
			this.beanInfo = beanInfo;
			this.socketInfo = socketInfo;
		}
		
		public BeanInfo getBeanInfo() {
			return beanInfo;
		}

		public SocketInfo getSocketInfo() {
			return socketInfo;
		}
	}
	
	private class CycleDetectionContext {
		private LinkedList<BeanInfo> beanStack = new LinkedList<>();
		private LinkedList<SocketInfo> socketStack = new LinkedList<>();
		
		private List<List<CycleInfo>> cycles = new ArrayList<>();
		
		private Map<BeanInfo, List<BeanInfo>> visitedBeans = new HashMap<>();
		
		public void addCycle(BeanInfo bean) {
			LinkedList<CycleInfo> cycle = new LinkedList<>();
			for(int i=0;i<this.beanStack.size();i++) {
				cycle.addFirst(new CycleInfo(this.beanStack.get(i), this.socketStack.get(i)));
				if(bean.equals(this.beanStack.get(i))) {
					break;
				}
			}
			this.cycles.add(cycle);
		}
		
		public List<List<CycleInfo>> getCycles() {
			return this.cycles;
		}
		
		public void pushBean(BeanInfo bean) {
			this.beanStack.push(bean);
		}
		
		public void pushSocket(SocketInfo socket) {
			this.socketStack.push(socket);
		}
		
		public void popBean() {
			this.beanStack.pop();
		}
		
		public void popSocket() {
			this.socketStack.pop();
		}
		
		public boolean isOnStack(BeanInfo bean) {
			return this.beanStack.contains(bean);
		}
		
		public boolean isVisited(BeanInfo bean) {
			if(this.visitedBeans.containsKey(bean) && !this.beanStack.isEmpty()) {
				return this.visitedBeans.get(bean).contains(this.beanStack.peek());
			}
			return false;
		}
		
		public void setVisited(BeanInfo bean) {
			if(!this.beanStack.isEmpty()) {
				List<BeanInfo> fromBeans = this.visitedBeans.get(bean);
				if(fromBeans == null) {
					fromBeans = new ArrayList<>();
					this.visitedBeans.put(bean,  fromBeans);
				}
				fromBeans.add(this.beanStack.peek());
			}
		}
	}
}
