package net.anotheria.anodoc.data;

/**
 * This property represents a long value.
 * @since 1.0
 */
public class LongProperty extends Property{
	
	/**
	 * svid.
	 */
	private static final long serialVersionUID = 2177663057004436401L;
	
	/**
     * Creates a new LongProperty with given name and value.
	 */
	public LongProperty(String name, long value){
		this(name, Long.valueOf(value));
	}
	
	/**
	 * Creates a new LongProperty with given name and value.
	 */
	public LongProperty(String name, Long value){
		super(name, value);
	}
	
	/**
	 * Returns the value of this property as Long object.
	 */
	public Long getLong(){
		return (Long)getValue();
	}
	
	/**
	 * Returns the value of this property as long.
	 */
	public long getlong(){
		return getLong().longValue();
	}

	/**
	 * Returns the value of this property as long.
	 */
	public long longValue(){
		return getLong().longValue();
	}
	
	/**
	 * Sets the value of this property to the given value.
	 */
	public void setLong(long aValue){
		setLong(Long.valueOf(aValue));
	}
	
	/**
	 * Sets the value of this property to the given Long object.
	 */
	public void setLong(Long aValue){
		super.setValue(aValue);
	}
	
	/**
	 * Sets the value of this property to the given value.
	 * @param o can be a Long or a String.
	 **/
	public void setValue(Object o){
		if (o instanceof Long){
			super.setValue(o);
			return;
		}
		if (o instanceof String){
			try{
				super.setValue(Long.valueOf((String)o));
				return;
			}catch(NumberFormatException nfe){
			}
		}
		throw new RuntimeException(o+" is not a legal value for LongProperty"); 
	}
	
	/**
	 * Returns the size in bytes (8bytes = 64 bit datatype).
	 * @see net.anotheria.anodoc.data.Property#getDataSize()
	 */
	@Override public long getSizeInBytes() {
		return 8;
	}
	
	/* (non-Javadoc)
	 * @see net.anotheria.anodoc.data.Property#cloneValue()
	 */
	@Override protected Object cloneValue() {
		return new Long(getlong());
	}

	@Override public PropertyType getPropertyType(){
		return PropertyType.LONG;
	}

}
