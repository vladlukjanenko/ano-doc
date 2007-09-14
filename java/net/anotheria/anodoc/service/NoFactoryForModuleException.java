package net.anotheria.anodoc.service;

/**
 * Thrown when the {@link biz.beaglesoft.bgldoc.service.IModuleService} needs 
 * to create a new module instance, but doesn't have the appropriate factory configured.
 */
public class NoFactoryForModuleException extends Exception{
	public NoFactoryForModuleException(String message){
		super(message);
	}

}
