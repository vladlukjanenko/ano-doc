package net.anotheria.asg.generator.meta;

/**
 * A property of enumeration type.
 * @author another
 */
public class MetaEnumerationProperty extends MetaProperty{
	
	/**
	 * Name of the enumeration.
	 */
	private String enumeration;
	
	/**
	 * Creates a new enumeration property.
	 * @param aName
	 * @param aType
	 */
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
