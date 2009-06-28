package net.anotheria.anodoc.data;

/**
 * This exception will be thrown if a not existing DataHolder was requested.
 */
public class NoSuchDataHolderException extends RuntimeException{
	public NoSuchDataHolderException(String name){
		super("No such data holder "+name);
	}
} 