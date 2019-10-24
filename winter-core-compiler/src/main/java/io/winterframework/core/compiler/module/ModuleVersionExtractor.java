package io.winterframework.core.compiler.module;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.ModuleElement;

import io.winterframework.core.compiler.spi.ModuleBeanInfo;
import io.winterframework.core.compiler.spi.ModuleInfo;
import io.winterframework.core.compiler.spi.ModuleInfoBuilder;
import io.winterframework.core.compiler.spi.ModuleQualifiedName;
import io.winterframework.core.compiler.spi.SocketBeanInfo;

public class ModuleVersionExtractor {

	private VersionModuleInfoBuilder versionModuleInfoBuilder;
	
	private class VersionModuleInfoBuilder extends AbstractModuleInfoBuilder {

		public VersionModuleInfoBuilder(ProcessingEnvironment processingEnvironment, ModuleElement moduleElement) {
			super(processingEnvironment, moduleElement);
		}
		
		@Override
		public ModuleQualifiedName getQualifiedName() {
			return this.moduleQName;
		}

		@Override
		public ModuleInfoBuilder beans(ModuleBeanInfo[] beans) {
			throw new UnsupportedOperationException();
		}

		@Override
		public ModuleInfoBuilder sockets(SocketBeanInfo[] sockets) {
			throw new UnsupportedOperationException();
		}

		@Override
		public ModuleInfoBuilder modules(ModuleInfo[] modules) {
			throw new UnsupportedOperationException();
		}

		@Override
		public ModuleInfo build() {
			throw new UnsupportedOperationException();
		}
	}
	
	public ModuleVersionExtractor(ProcessingEnvironment processingEnvironment, ModuleElement moduleElement) {
		this.versionModuleInfoBuilder = new VersionModuleInfoBuilder(processingEnvironment, moduleElement);
	}

	public Integer getModuleVersion() {
		return this.versionModuleInfoBuilder.version;
	}
	
	public ModuleQualifiedName getModuleQualifiedName() {
		return this.versionModuleInfoBuilder.moduleQName;
	}
}
