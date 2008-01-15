package net.anotheria.asg.exception;

/**
 * Base class for all exceptions thrown by generated code at runtime.
 * @author another
 *
 */
public class ASGRuntimeException extends Exception{
	public ASGRuntimeException(String message){
		super(message);
	}
	
	public ASGRuntimeException(Throwable cause){
		super(cause);
	}
}
