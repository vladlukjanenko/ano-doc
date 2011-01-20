package net.anotheria.anodoc.service;

/**
 * Thrown when the {@link net.anotheria.anodoc.service.IModuleService} needs 
 * to create a new module instance, but doesn't have the appropriate factory configured.
 */
public class NoFactoryForModuleException extends Exception{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public NoFactoryForModuleException(String message){
		super(message);
	}

}
