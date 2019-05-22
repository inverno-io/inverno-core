/**
 * 
 */
package io.winterframework.core.compiler.spi;

/**
 * @author jkuhn
 *
 */
public class BeanQualifiedName extends QualifiedName {

	private ModuleQualifiedName moduleQName;
	
	private String beanName;
	
	public BeanQualifiedName(ModuleQualifiedName moduleQName, String beanName) throws QualifiedNameFormatException {
		super(moduleQName.getValue() + SEPARATOR + beanName);
		
		this.moduleQName = moduleQName;
		
		this.validateQualifiedNamePart(beanName);
		this.beanName = beanName;
	}
	
	@Override
	public String getSimpleValue() {
		return this.getBeanName();
	}
	
	public ModuleQualifiedName getModuleQName() {
		return this.moduleQName;
	}
	
	public String getBeanName() {
		return this.beanName;
	}
	
	public static BeanQualifiedName valueOf(String qname) throws QualifiedNameFormatException {
		int lastSeparatorIndex = qname.lastIndexOf(SEPARATOR);
		if(lastSeparatorIndex == -1) {
			throw new QualifiedNameFormatException("Invalid qname " + qname + ", was expecting: ModuleQualifiedName():<beanName>");
		}
		return new BeanQualifiedName(ModuleQualifiedName.valueOf(qname.substring(0, lastSeparatorIndex)), qname.substring(lastSeparatorIndex+1));
	}
}
