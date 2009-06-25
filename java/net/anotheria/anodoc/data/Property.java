package net.anotheria.anodoc.data;

import net.anotheria.util.xml.XMLAttribute;
import net.anotheria.util.xml.XMLNode;

/**
 * This class represents a single data entry which can be stored.
 * It is of a plain kind (can't contain any subproperties, except
 * the ListProperty).
 * @author lro
 * @since 1.0
 */
public abstract class Property
	 extends DataHolder implements IPlainDataObject,Cloneable{
	 	
	private static final long serialVersionUID = -4170023770710073469L;

	/**
	 * The saveable value of the object.
	 */
	private Object value;
	
	/**
	 * Creates new Property without a value (null).
	 */
	protected Property(String name){
		this(name, null);
	}
	
	/**
	 * Creates a new Property with a given name and given value.
	 */
	protected Property(String name, Object aValue){
		super(name);
		value = aValue;	
	}

	/**
	 * Returns the value of this property.
	 */	
	public Object getValue(){
		return value;
	
	}
	
	/**
	 * Sets the value of this Property.
	 */
	public void setValue(Object o){
		this.value = o;
	}
	
	/**
	 * A simple string representation of the Property,
	 * name=value.
	 * 
	 */
	public String toString(){
		return getPropertyType().getIndicator()+getName()+"="+value;
	}
	
	/**
	 * Since the can't be two equally named properties in a Document, 
	 * the storageId of a Property is its name.
	 * @see net.anotheria.anodoc.data.IBasicStoreableObject#getStorageId()
	 */
	public String getStorageId() {
		return getName();
	}
	
	@Override public boolean equals(Object o){
		if (!(o instanceof Property))
			return false;
		Property anotherProperty = (Property)o;
		return getName().equals(anotherProperty.getName()) && 
			   value.equals(anotherProperty.value); 
	}
	
	protected String getName(){
		return getId();
	}
	
	public Object clone() throws CloneNotSupportedException{
		Property newP = (Property)super.clone();
		newP.setValue(cloneValue());
		return newP;
	}
	
	public Property cloneAs(String newName) throws CloneNotSupportedException{
		Property newP = (Property)super.clone();
		newP.setValue(cloneValue());
		newP.setId(newName);
		return newP;
	}
	
	protected abstract Object cloneValue() throws CloneNotSupportedException;
	
	public XMLNode toXMLNode(){
		XMLNode ret = new XMLNode("property");
		ret.addAttribute(new XMLAttribute("name", getName()));
		ret.addAttribute(new XMLAttribute("type", getPropertyType().toString()));
		ret.setContent(""+getValue());
		return ret;
	}
	
	public abstract PropertyType getPropertyType();
	
}
