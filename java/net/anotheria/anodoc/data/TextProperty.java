package net.anotheria.anodoc.data;



/**
 * TextProperty is a derivative of a StringProperty which support full text search queries 
 * (if the underlying storage supports them too).
 */
public class TextProperty extends StringProperty {

	/**
	 * Creates a new TextProperty with given name and null value. 
	 */
	public TextProperty(String name) {
		super(name);
	}

	/**
	 * Creates a new TextProperty with given name and value. 
	 */
	public TextProperty(String name, String value) {
		super(name, value);
	}

	/**
	 * Returns the type indicator of this property - 'T' (for differencing between
	 * StringProperty and TextProperty.  
	 */
	protected char getIndicator(){
		return 'T';
	}
	
	public PropertyType getPropertyType(){
		return PropertyType.TEXT;
	}

}
 