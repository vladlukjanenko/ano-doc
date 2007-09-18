package net.anotheria.asg.data;
/**
 * Basic interface which all objects managed by the generated code will implement and which provides basic access methods.
 * @author another
 *
 */
public interface DataObject extends Cloneable{
	public String getId();

	public Object clone() throws CloneNotSupportedException;

	/**
	 * Returns the value of a property in an underlying type.
	 * @param propertyName
	 * @return
	 */
	public Object getPropertyValue(String propertyName);
	
	/**
	 * Returns the name under which the object was defined in xml originally.
	 * @return
	 */
	public String getDefinedName();

}
