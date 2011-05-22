package net.anotheria.anodoc.data;

/**
 * This exception will be thrown when a not existing Property was requested,
 * or the DataHolder with correspoding name is not a Property.
 * I.e. you if you call getProperty("foo") and "foo" is the name of a Document in this context.
 */
public class NoSuchPropertyException extends RuntimeException{
	/**
	 * SerialVersionUID.
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * Creates a new instance.
	 * @param name name of the property.
	 * @param type type of the property.
	 */
	public NoSuchPropertyException(String name, String type){
		super("No such property "+name+", or "+name+" is not a "+type);
	}

}
