package net.anotheria.anodoc.util;

/**
 * Base exception class for this service, custom exceptions should derive from it.
 */

public class CommonModuleStorageException extends Exception{
	public CommonModuleStorageException(String aMessage){
		super(aMessage);
	}
}
