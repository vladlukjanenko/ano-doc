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
	public MetaEnumerationProperty(String aName, MetaProperty.Type aType){
		super(aName, aType);
	}

	/**
	 * @return name of the enumeration
	 */
	public String getEnumeration() {
		return enumeration;
	}

	/**
	 * @param string name of enumeration
	 */
	public void setEnumeration(String string) {
		enumeration = string;
	}

}
