package net.anotheria.asg.generator.meta;

/**
 * TODO please remined another to comment this class
 * @author another
 */
public class MetaEnumerationProperty extends MetaProperty{
	
	private String enumeration;
	
	public MetaEnumerationProperty(String aName, String aType){
		super(aName, aType);
	}

	/**
	 * @return
	 */
	public String getEnumeration() {
		return enumeration;
	}

	/**
	 * @param string
	 */
	public void setEnumeration(String string) {
		enumeration = string;
	}

}
