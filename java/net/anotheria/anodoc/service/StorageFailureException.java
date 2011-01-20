package net.anotheria.anodoc.service;

import net.anotheria.anodoc.util.CommonModuleStorageException;

/**
 * General exception a service can throw if it can't proceed as desired.
 */
public class StorageFailureException extends CommonModuleStorageException{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public StorageFailureException(String message){
		super(message);
	}

}
