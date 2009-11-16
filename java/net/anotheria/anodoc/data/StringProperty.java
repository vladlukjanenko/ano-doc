package net.anotheria.anodoc.data;

/**
 * This property represents a string value.
 */
public class StringProperty extends Property{
	/**
	 * svid.
	 */
	private static final long serialVersionUID = -7506812728258106500L;
	
	/**
 	 * Creates a new StringProperty with given name and null value.
	 */
	public StringProperty(String name){
		this(name, null);
	}
	
	/**
	 * Creates a new StringProperty with given name and value.
	 */
	public StringProperty(String name, String value){
		super(name, value);
	}
	
	/**
	 * Returns the value of this property as String.
	 */
	public String getString(){
		return (String) getValue();
	}
	
	/**
	 * Sets the value of this property to the given String. 
	 */
	public void setString(String aString){
		setValue(aString);
	}
	
	/**
	 * Sets the value of this property to the String representation 
	 * of the given Object o. If o is null, the value of this property will
	 * be 'null'.
	 */
	@Override public void setValue(Object o){
		super.setValue(""+o);			
	}

	/**
	 * Returns true if the given obj is a StringProperty and the name, value tuples are equal.  
	 */
	@Override public boolean equals(Object obj) {
		if(obj instanceof StringProperty){
			StringProperty p = (StringProperty)obj;
			return p.getName().equals(getName()) && p.getValue().equals(getValue());
		}
		return false;
	}

	/**
	 * Returns the amount of bytes needed to save this property.
	 * @see net.anotheria.anodoc.data.Property#getSizeInBytes()
	 */
	@Override public long getSizeInBytes() { 
		String s = getString();
		return s==null ? 0 : s.length()*2;
	}
	
	@Override protected Object cloneValue() {
		return getString() != null? new String(getString()): null;
	}
	
	@Override public PropertyType getPropertyType(){
		return PropertyType.STRING;
	}

	

}
