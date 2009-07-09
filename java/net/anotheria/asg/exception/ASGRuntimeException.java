package net.anotheria.asg.exception;

/**
 * Base class for all exceptions thrown by generated code at runtime.
 * @author another
 *
 */
public class ASGRuntimeException extends Exception{
	/**
	 * Creates a new exception with a message.
	 * @param message
	 */
	public ASGRuntimeException(String message){
		super(message);
	}
	/**
	 * Creates a new exception with  a cause.
	 * @param cause
	 */
	public ASGRuntimeException(Throwable cause){
		super(cause);
	}
	/**
	 * Creates a new exception with a message and a cause.
	 * @param cause
	 */
	public ASGRuntimeException(String message, Throwable cause){
		super(message, cause);
	}
}
