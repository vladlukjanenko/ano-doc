package net.anotheria.anodoc.data;

/**
 * This class represents a double property (the mapping for double attributes).
 * @since 1.1
 */
public class DoubleProperty extends Property{
	/**
	 * svid.
	 */
	private static final long serialVersionUID = 5268122271480317179L;
	
	/**
	 * Creates a new DoubleProperty with given name and value.
	 */
	public DoubleProperty(String name, double value){
		this(name, new Double(value));
	}
	
	/**
	 * Creates a new DoubleProperty with given name and value.
	 */
	public DoubleProperty(String name, Double value){
		super(name, value);
	}

	/**
	 * @return the value of this property as Float object.
	 */	
	public Double getDouble(){
		return (Double)getValue();
	}
	
	/**
	 * @return the value of this property as float.
	 */	
	public double getdouble(){
		return getDouble().doubleValue();
	}
	
	/**
	 * Sets the value of this property to given float value.
	 */	
	public void setFloat(double aValue){
		setDouble(new Double(aValue));
	}
	
	/**
	 * Sets the value of this property to given Float value.
	 */	
	public void setDouble(Double aValue){
		super.setValue(aValue);
	}
	
	/**
	 * Sets the value of this property to given value, whether the value 
	 * can be a Float or a String.
	 */	
	@Override public void setValue(Object o){
		if (o instanceof Double){
			super.setValue(o);
			return;
		}
		if (o instanceof String){
			try{
				super.setValue(new Double( (String)o));
				return;
			}catch(NumberFormatException nfe){
			}
		}
		throw new RuntimeException(o+" is not a legal value for DoubleProperty"); 
	}

	/**
	 * @return the size needed to hold a float value in bytes (8).
	 * @see net.anotheria.anodoc.data.Property#getSizeInBytes()
	 */
	@Override public long getSizeInBytes() {
		return 16;
	}
	
	/* (non-Javadoc)
	 * @see net.anotheria.anodoc.data.Property#cloneValue()
	 */
	@Override protected Object cloneValue() {
		return new Double(getdouble());
	}
	 
	@Override public PropertyType getPropertyType(){
		return PropertyType.DOUBLE;
	}
}


