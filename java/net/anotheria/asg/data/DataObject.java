package net.anotheria.asg.data;

import net.anotheria.util.xml.XMLNode;

/**
 * Basic interface which all objects managed by the generated code will implement and which provides basic access methods.
 * @author lrosenberg
 *
 */
public interface DataObject extends Cloneable{
	/**
	 * Returns a document id. Each document regardless if it comes from db or filesystem has a unique id.
	 * @return
	 */
	String getId();

	/**
	 * DataObjects are cloneable
	 * @return
	 * @throws CloneNotSupportedException
	 */
	Object clone() throws CloneNotSupportedException;

	/**
	 * Returns the value of a property in an underlying type.
	 * @param propertyName
	 * @return
	 */
	Object getPropertyValue(String propertyName);
	
	/**
	 * Returns the name under which the object was defined in xml originally.
	 * @return
	 */
	String getDefinedName();

	/**
	 * Returns the timestamp (time in ms since 1970) of the last change.
	 * @return
	 */
	long getLastUpdateTimestamp();

	/**
	 * Creates an XMLNode for this document for XML export.
	 * @return
	 */
	XMLNode toXMLNode();
	
	//public void copyAttributesFrom(DataObject object);
	
	/**
	 * Returns the footprint of the document. The footprint is the unique code which identifies a document state. The footprint is calculates from the values of 
	 * all attributes.
	 */
	String getFootprint();
	/**
	 * Returns the object info about this data object.
	 * @return
	 */
	ObjectInfo getObjectInfo();

}
 