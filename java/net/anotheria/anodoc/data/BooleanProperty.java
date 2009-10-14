package net.anotheria.anodoc.data;

/**
 * This class represents a boolean property.
 * @since 1.0
 * @author lrosenberg
 */
public class BooleanProperty extends Property{
	/**
	 * svid.
	 */
	private static final long serialVersionUID = -6112656517280319094L;
	
	/**
	 * Creates a new BooleanProperty with given name and value. 
	 */
	public BooleanProperty(String name, boolean value){
		this(name, Boolean.valueOf(value));
	}
	
	/**
	 * Creates a new BooleanProperty with given name and value. 
	 */
	public BooleanProperty(String name, Boolean value){
		super(name, value);
	}
	
	/**
	 * Returns the value of this Property as a Boolean object.
	 */
	public Boolean getBoolean(){
		return (Boolean)getValue();
	}
	
	/**
	 * Returns the value of this Property as boolean data (primary type).
	 */
	public boolean getboolean(){
		return getBoolean().booleanValue();
	}
	
	/**
	 * Sets the value of this property to the given boolean value.
	 */
	public void setboolean(boolean aValue){
		setBoolean(Boolean.valueOf(aValue));
	}
	
	/**
	 * Sets the value of this property to the given Boolean value.
	 */
	public void setBoolean(Boolean aValue){
		super.setValue(aValue);
	}
	
	/**
	 * Sets the value of this property to the given value, which can be a Boolean or a String.
	 */
	@Override public void setValue(Object o){
		if (o instanceof Boolean){
			super.setValue(o);
			return;
		}
		if (o instanceof String){
			try{
				super.setValue(Boolean.valueOf((String)o));
				return;
			}catch(NumberFormatException nfe){
			}
		}
		throw new RuntimeException(o+" is not a legal value for BooleanProperty"); 
	}
	
	/**
	 * Returns the size of this property in bytes (one byte).
	 * @see net.anotheria.anodoc.data.Property#getSizeInBytes()
	 */
	@Override public long getSizeInBytes() {
		return 1;
	}
	
	@Override protected Object cloneValue() {
		return getboolean() ? Boolean.TRUE : Boolean.FALSE;
	}
	
	@Override public PropertyType getPropertyType(){
		return PropertyType.BOOLEAN;
	}

}
