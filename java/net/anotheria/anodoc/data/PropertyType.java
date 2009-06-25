package net.anotheria.anodoc.data;
/**
 * Declaration of supported types for properties.
 * @author lrosenberg
 *
 */
public enum PropertyType {
	/**
	 * Integer.
	 */
	INT('I'),
	/**
	 * Long.
	 */
	LONG('L'),
	/**
	 * Double.
	 */
	DOUBLE('D'),
	/**
	 * Float.
	 */
	FLOAT('F'),
	/**
	 * List of properties.
	 */
	LIST('['),
	/**
	 * String.
	 */
	STRING('S'),
	/**
	 * Text. Same as String but with different editors.
	 */
	TEXT('T'),
	/*
	 * Boolean.
	 */
	BOOLEAN('B');
	
	private char indicator;
	
	private PropertyType(char anIndicator){
		indicator = anIndicator;
	}
	
	public char getIndicator(){
		return indicator;
	}
	
	
}
