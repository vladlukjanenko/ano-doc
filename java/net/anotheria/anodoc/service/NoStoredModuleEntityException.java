package net.anotheria.anodoc.service;

import net.anotheria.anodoc.util.CommonModuleStorageException;

/**
 * Thrown when the {@link net.anotheria.anodoc.service.IModuleService} needs 
 * to load a module instance, but it isn't stored.
 * If the load method was called on {@link net.anotheria.anodoc.service.IModuleService}
 * with the create flag, this exception will be caught by the service and a new instance created.
 */
public class NoStoredModuleEntityException extends CommonModuleStorageException{
	/**
	 * Basic serial version UID.
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * Default constructor.
	 * @param message message of exception
	 */
	public NoStoredModuleEntityException(String message){
		super(message);
	}

}
