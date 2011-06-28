package net.anotheria.anodoc.data;

/**
 * This exception will be thrown if a not existing DataHolder was requested.
 */
public class NoSuchDataHolderException extends RuntimeException{
	/**
	 * SerialVersionUID.
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * Creates a new exception with the given data holder name.
	 * @param name
	 */
	public NoSuchDataHolderException(String name){
		super("No such data holder "+name);
	}
}
