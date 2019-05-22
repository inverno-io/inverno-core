/**
 * 
 */
package io.winterframework.core.compiler.spi;

/**
 * @author jkuhn
 *
 */
public class BeanSocketQualifiedName extends QualifiedName {

	private BeanQualifiedName beanQName;
	
	private String name;
	
	public BeanSocketQualifiedName(BeanQualifiedName beanQName, String name) throws QualifiedNameFormatException {
		super(beanQName.getValue() + SEPARATOR + name);
		
		this.beanQName = beanQName;
		this.name = name;
		
		this.validateQualifiedNamePart(this.name);
	}
	
	@Override
	public String getSimpleValue() {
		return this.getName();
	}
	
	public BeanQualifiedName getBeanQName() {
		return this.beanQName;
	}
	
	public String getName() {
		return this.name;
	}
	
	public static BeanSocketQualifiedName valueOf(String qname) throws QualifiedNameFormatException {
		int lastSeparatorIndex = qname.lastIndexOf(SEPARATOR);
		if(lastSeparatorIndex == -1) {
			throw new QualifiedNameFormatException("Invalid qname "+ qname +", was expecting: BeanQualifiedName():<socketName>");
		}
		return new BeanSocketQualifiedName(BeanQualifiedName.valueOf(qname.substring(0, lastSeparatorIndex)), qname.substring(lastSeparatorIndex+1));
	}
	
	public static BeanSocketQualifiedName valueOf(ModuleQualifiedName moduleQName, String qname) throws QualifiedNameFormatException {
		String[] qnameParts = qname.split(SEPARATOR);
		if(qnameParts.length != 2) {
			throw new QualifiedNameFormatException("Invalid qname "+ qname +", was expecting: <beanName>:<socketName>");
		}
		return new BeanSocketQualifiedName(new BeanQualifiedName(moduleQName, qnameParts[0]), qnameParts[1]);
	}
}
