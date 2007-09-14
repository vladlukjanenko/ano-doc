package net.anotheria.asg.data;

public interface DataObject extends Cloneable{
	public String getId();

	public Object clone() throws CloneNotSupportedException;
	
	public Object getPropertyValue(String propertyName);
	
	public String getDefinedName();

}
