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
package io.inverno.core.compiler.spi;

/**
 * <p>
 * A qualified name identifying a module.
 * </p>
 *
 * <p>
 * The raw representation of a module qualified name is a valid Java name.
 * </p>
 *
 * <p>
 * The name of a module is tightly linked to the name of the corresponding Java module.
 * </p>
 * 
 * @author <a href="mailto:jeremy.kuhn@inverno.io">Jeremy Kuhn</a>
 *
 */
public class ModuleQualifiedName extends QualifiedName {

	/**
	 * <p>
	 * The simple name of the module which corresponds to the last part of the Java module name.
	 * </p>
	 */
	private String moduleName;

	/**
	 * <p>
	 * The package name of the module.
	 * </p>
	 */
	private String packageName;

	/**
	 * <p>
	 * The name of the class used to run a module.
	 * </p>
	 */
	private String className;
	
	/**
	 * <p>
	 * The source package name where to place generated source files.
	 * </p>
	 */
	private String sourcePackageName;

	/**
	 * <p>
	 * Creates a module qualified name from the specified package and module names.
	 * </p>
	 * 
	 * @param packageName the package name
	 * @param moduleName  the simple name of the module
	 * 
	 * @throws QualifiedNameFormatException if one of the specified names is invalid
	 */
	public ModuleQualifiedName(String packageName, String moduleName) throws QualifiedNameFormatException {
		this(packageName, moduleName, null, null);
	}

	/**
	 * <p>
	 * Creates a module qualified name from the specified package and module names with the specified class name.
	 * </p>
	 *
	 * @param packageName the package name
	 * @param moduleName  the module simple name
	 * @param className   the module class name
	 *
	 * @throws QualifiedNameFormatException if one of the specified names is invalid
	 */
	public ModuleQualifiedName(String packageName, String moduleName, String className)
			throws QualifiedNameFormatException {
		this(packageName, moduleName, className, null);
	}

	/**
	 * <p>
	 * Creates a module qualified name from the specified package and module names with the specified class name and source package name.
	 * </p>
	 * 
	 * @param packageName       the package name
	 * @param moduleName        the module simple name
	 * @param className         the module class name
	 * @param sourcePackageName the module source package name
	 * 
	 * @throws QualifiedNameFormatException if one of the specified names is invalid
	 */
	public ModuleQualifiedName(String packageName, String moduleName, String className, String sourcePackageName)
			throws QualifiedNameFormatException {
		super((packageName == null || packageName.equals("") ? "" : packageName + ".") + moduleName);

		this.packageName = packageName == null ? "" : packageName;
		this.moduleName = moduleName;
		this.className = className;
		this.sourcePackageName = sourcePackageName == null ? "" : sourcePackageName;
		
		this.validatePackageName(this.packageName);
		this.validatePackageName(this.sourcePackageName);
		this.validateQualifiedNamePart(this.moduleName);
	}

	/**
	 * <p>
	 * Validates that the specified package name is a valid Java package name.
	 * </p>
	 * 
	 * @param packageName the package name to validate
	 * 
	 * @throws QualifiedNameFormatException if the specified package name is invalid
	 */
	private void validatePackageName(String packageName) throws QualifiedNameFormatException {
		if (!packageName.equals("")) {
			for (String packagePart : packageName.split("\\.")) {
				boolean start = true;
				for (char c : packagePart.toCharArray()) {
					if (start) {
						if (!Character.isJavaIdentifierStart(c)) {
							throw new QualifiedNameFormatException(
									"Package name must be a valid Java package name: " + packageName);
						}
						start = false;
					} else {
						if (!Character.isJavaIdentifierPart(c)) {
							throw new QualifiedNameFormatException(
									"Package name must be a valid Java package name: " + packageName);
						}
					}
				}
			}
		}
	}

	/**
	 * <p>
	 * Returns the module class name.
	 * </p>
	 *
	 * <p>
	 * If not explicitly specified, the class name derives from the module package name and the module simple name for the package and uses the module simple name for the simple class name (eg. the
	 * class name of module <code>com.example.someModule</code> is <code>com.example.someModule.SomeModule</code>.
	 * </p>
	 *
	 * @return a canonical class name
	 */
	public String getClassName() {
		if (this.className != null && !this.className.equals("")) {
			return this.className;
		}
		return this.getSourcePackageName() + "." + Character.toUpperCase(this.getModuleName().charAt(0))
				+ this.getModuleName().substring(1);
	}

	@Override
	public String getSimpleValue() {
		return this.getModuleName();
	}

	/**
	 * <p>
	 * Return the source package name.
	 * </p>
	 * 
	 * @return the source package name.
	 */
	public String getSourcePackageName() {
		if(this.sourcePackageName != null && !this.sourcePackageName.equals("")) {
			return this.sourcePackageName;
		}
		return this.getValue();
	}
	
	/**
	 * <p>
	 * Returns the module package name.
	 * </p>
	 * 
	 * @return a package name
	 */
	public String getPackageName() {
		return this.packageName;
	}

	/**
	 * <p>
	 * Returns the module simple name.
	 * </p>
	 * 
	 * <p>
	 * Note that this representation is not "qualified".
	 * </p>
	 * 
	 * @return a simple name
	 */
	public String getModuleName() {
		return this.moduleName;
	}

	/**
	 * <p>
	 * Creates a module qualified name from the specified raw value which should be a valid Java name.
	 * </p>
	 *
	 * @param qname a raw qualified name
	 *
	 * @return a module qualified name
	 *
	 * @throws QualifiedNameFormatException if the specified value is not a module qualified name
	 */
	public static ModuleQualifiedName valueOf(String qname) throws QualifiedNameFormatException {
		return valueOf(qname, null);
	}

	/**
	 * <p>
	 * Creates a module qualified name from the specified raw value which should be a valid Java name and with the specified class name.
	 * </p>
	 *
	 * @param qname     a raw qualified name
	 * @param className a module class name
	 *
	 * @return a module qualified name
	 *
	 * @throws QualifiedNameFormatException if the specified value is not a module qualified name
	 */
	public static ModuleQualifiedName valueOf(String qname, String className) throws QualifiedNameFormatException {
		return valueOf(qname, className, null);
	}
	
	/**
	 * <p>
	 * Creates a module qualified name from the specified raw value which should be a valid Java name and with the specified class name.
	 * </p>
	 *
	 * @param qname             a raw qualified name
	 * @param className         a module class name
	 * @param sourcePackageName a module source package name
	 *
	 * @return a module qualified name
	 *
	 * @throws QualifiedNameFormatException if the specified value is not a module qualified name
	 */
	public static ModuleQualifiedName valueOf(String qname, String className, String sourcePackageName) throws QualifiedNameFormatException {
		int lastDotIndex = qname.lastIndexOf(".");
		if (lastDotIndex > -1) {
			return new ModuleQualifiedName(qname.substring(0, lastDotIndex), qname.substring(lastDotIndex + 1), className, sourcePackageName);
		} 
		else {
			return new ModuleQualifiedName("", qname, className, sourcePackageName);
		}
	}
}
