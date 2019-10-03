/**
 * 
 */
package io.winterframework.core.compiler.spi;

/**
 * @author jkuhn
 *
 */
public class ModuleQualifiedName extends QualifiedName {

	private String moduleName;
	
	private String packageName;
	
	private String className;
	
	public ModuleQualifiedName(String packageName, String moduleName) throws QualifiedNameFormatException {
		this(packageName, moduleName, null);
	}
	
	public ModuleQualifiedName(String packageName, String moduleName, String className) throws QualifiedNameFormatException {
		super((packageName == null || packageName.equals("") ? "" : packageName + ".") + moduleName);

		this.packageName = packageName == null ? "" : packageName;
		this.moduleName = moduleName;
		this.className = null;
		
		this.validatePackageName(this.packageName);
		this.validateQualifiedNamePart(this.moduleName);
	}
	
	private void validatePackageName(String packageName) throws QualifiedNameFormatException {
		if(!packageName.equals("")) {
			for(String packagePart : packageName.split("\\.")) {
				boolean start = true;
				for (char c : packagePart.toCharArray()) {
					if(start) {
						if(!Character.isJavaIdentifierStart(c)) {
							throw new QualifiedNameFormatException("Package name must be a valid Java package name: " + packageName);
						}
						start = false;
					}
					else {
						if(!Character.isJavaIdentifierPart(c)) {
							throw new QualifiedNameFormatException("Package name must be a valid Java package name: " + packageName);
						}
					}
				}
			}
		}
	}
	
	public String getClassName() {
		if(this.className != null && !this.className.equals("")) {
			return this.className;
		}
		return this.getValue() + "." + Character.toUpperCase(this.getModuleName().charAt(0)) + this.getModuleName().substring(1);
	}
	
	@Override
	public String getSimpleValue() {
		return this.getModuleName();
	}
	
	public String getPackageName() {
		return this.packageName;
	}
	
	public String getModuleName() {
		return this.moduleName;
	}
	
	public static ModuleQualifiedName valueOf(String qname) throws QualifiedNameFormatException {
		return valueOf(qname, null);
	}
	
	public static ModuleQualifiedName valueOf(String qname, String className) throws QualifiedNameFormatException {
		int lastDotIndex = qname.lastIndexOf(".");
		if(lastDotIndex > -1) {
			return new ModuleQualifiedName(qname.substring(0, lastDotIndex), qname.substring(lastDotIndex + 1), className);
		}
		else {
			return new ModuleQualifiedName("", qname, className);
		}
	}
}
