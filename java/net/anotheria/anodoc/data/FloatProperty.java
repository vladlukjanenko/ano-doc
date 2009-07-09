package net.anotheria.anodoc.data;

/**
 * This class represents a float property (the mapping for float attributes)
 * @since 1.0
 */
public class FloatProperty extends Property{
	
	/**
	 * svid.
	 */
	private static final long serialVersionUID = -8097948895367514824L;

	/**
	 * Creates a new FloatProperty with given name and value.
	 */
	public FloatProperty(String name, float value){
		this(name, new Float(value));
	}
	
	/**
	 * Creates a new FloatProperty with given name and value.
	 */
	public FloatProperty(String name, Float value){
		super(name, value);
	}

	/**
	 * Returns the value of this property as Float object.
	 */	
	public Float getFloat(){
		return (Float)getValue();
	}
	
	/**
	 * Returns the value of this property as float.
	 */	
	public float getfloat(){
		return getFloat().floatValue();
	}
	
	/**
	 * Sets the value of this property to given float value.
	 */	
	public void setFloat(float aValue){
		setFloat(new Float(aValue));
	}
	
	/**
	 * Sets the value of this property to given Float value.
	 */	
	public void setFloat(Float aValue){
		super.setValue(aValue);
	}
	
	/**
	 * Sets the value of this property to given value, whether the value 
	 * can be a Float or a String.
	 */	
	@Override public void setValue(Object o){
		if (o instanceof Float){
			super.setValue(o);
			return;
		}
		if (o instanceof String){
			try{
				super.setValue(new Float( (String)o));
				return;
			}catch(NumberFormatException nfe){
			}
		}
		throw new RuntimeException(o+" is not a legal value for FloatProperty"); 
	}

	/**
	 * Returns the size needed to hold a float value in bytes (8).
	 * @see net.anotheria.anodoc.data.Property#getSizeInBytes()
	 */
	@Override public long getSizeInBytes() {
		return 8;
	}
	
	/* (non-Javadoc)
	 * @see net.anotheria.anodoc.data.Property#cloneValue()
	 */
	@Override protected Object cloneValue() {
		return new Float(getfloat());
	}

	@Override public PropertyType getPropertyType(){
		return PropertyType.FLOAT;
	}

}
