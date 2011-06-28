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
	 * @return document id
	 */
	String getId();

	/**
	 * DataObjects are cloneable.
	 * @return cloned object
	 * @throws CloneNotSupportedException
	 */
	Object clone() throws CloneNotSupportedException;

	/**
	 * Returns the value of a property in an underlying type.
	 * @param propertyName
	 * @return value of property
	 */
	Object getPropertyValue(String propertyName);
	
	/**
	 * Returns the name under which the object was defined in xml originally.
	 * @return name of definition
	 */
	String getDefinedName();
	
	/**
	 * Returns the parent module name under which the object was defined in xml originally.
	 * @return parent module
	 */
	String getDefinedParentName();

	/**
	 * Returns the timestamp (time in ms since 1970) of the last change.
	 * @return timestamp
	 */
	long getLastUpdateTimestamp();

	/**
	 * Creates an XMLNode for this document for XML export.
	 * @return created XMLSNode
	 */
	XMLNode toXMLNode();
	
	//public void copyAttributesFrom(DataObject object);
	
	/**
	 * Returns the footprint of the document. The footprint is the unique code which identifies a document state. The footprint is calculates from the values of 
	 * all attributes.
	 * @return footprint
	 */
	String getFootprint();
	/**
	 * Returns the object info about this data object.
	 * @return object info
	 */
	ObjectInfo getObjectInfo();

}
