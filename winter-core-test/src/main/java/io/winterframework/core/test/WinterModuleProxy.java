/**
 * 
 */
package io.winterframework.core.test;

import java.lang.reflect.InvocationTargetException;

import io.winterframework.core.Module;

/**
 * @author jkuhn
 *
 */
public class WinterModuleProxy extends Module {

	private Object module;
	
	public WinterModuleProxy(Object module) {
		super(null);
		this.module = module;
	}

	@Override
	public String getName() {
		try {
			return (String)this.module.getClass().getMethod("getName").invoke(this.module);
		} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException
				| SecurityException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void start() {
		try {
			this.module.getClass().getMethod("start").invoke(this.module);
		} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException
				| SecurityException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void stop() {
		try {
			this.module.getClass().getMethod("stop").invoke(this.module);
		} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException
				| SecurityException e) {
			throw new RuntimeException(e);
		}
	}
	
	public Object getBean(String name) {
		try {
			return this.module.getClass().getMethod(name).invoke(this.module);
		} catch (NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
			throw new RuntimeException(e);
		}
	}
}
