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
package io.winterframework.test;

import java.lang.reflect.InvocationTargetException;

import io.winterframework.core.v1.Module;

/**
 * @author <a href="mailto:jeremy.kuhn@winterframework.io">Jeremy Kuhn</a>
 *
 */
public class WinterModuleProxy extends Module {

	private Object module;
	
	public WinterModuleProxy(Object module) {
		super(null);
		this.module = module;
	}

	@Override
	public String getName() throws WinterModuleException {
		try {
			return (String)this.module.getClass().getMethod("getName").invoke(this.module);
		} 
		catch(InvocationTargetException e) {
			throw new WinterModuleException(e.getCause());
		}
		catch (IllegalAccessException | IllegalArgumentException | NoSuchMethodException
				| SecurityException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void start() throws WinterModuleException {
		try {
			this.module.getClass().getMethod("start").invoke(this.module);
		} 
		catch (InvocationTargetException e) {
			throw new WinterModuleException(e.getCause());
		}
		catch (IllegalAccessException | IllegalArgumentException | NoSuchMethodException
				| SecurityException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void stop() throws WinterModuleException {
		try {
			this.module.getClass().getMethod("stop").invoke(this.module);
		}
		catch (InvocationTargetException e) {
			throw new WinterModuleException(e.getCause());
		}
		catch (IllegalAccessException | IllegalArgumentException | NoSuchMethodException
				| SecurityException e) {
			throw new RuntimeException(e);
		}
	}
	
	public Object getBean(String name) throws WinterModuleException {
		try {
			return this.module.getClass().getMethod(name).invoke(this.module);
		}
		catch (InvocationTargetException e) {
			throw new WinterModuleException(e.getCause());
		}
		catch (NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException e) {
			throw new RuntimeException(e);
		}
	}
}
