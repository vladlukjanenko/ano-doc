package net.anotheria.anodoc.util;

/**
 * Base exception class for this service, custom exceptions should derive from it.
 */

public class CommonModuleStorageException extends Exception{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public CommonModuleStorageException(String aMessage){
		super(aMessage);
	}
}
