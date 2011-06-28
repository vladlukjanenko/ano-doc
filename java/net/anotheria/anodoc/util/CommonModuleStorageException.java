package net.anotheria.anodoc.util;

/**
 * Base exception class for this service, custom exceptions should derive from it.
 */

public class CommonModuleStorageException extends Exception{
	/**
	 * Basic serial version UID.
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * Default constructor.
	 * @param aMessage message of exception
	 */
	public CommonModuleStorageException(String aMessage){
		super(aMessage);
	}
}
