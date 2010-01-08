package net.anotheria.asg.exception;

/**
 * Base class for all exceptions thrown by generated code at runtime.
 * @author another
 *
 */
public class ConstantNotFoundException extends ASGRuntimeException{
	/**
	 * Creates a new exception with a message.
	 * @param message
	 */
	public ConstantNotFoundException(String message){
		super(message);
	}
	/**
	 * Creates a new exception with  a cause.
	 * @param cause
	 */
	public ConstantNotFoundException(Throwable cause){
		super(cause);
	}
	/**
	 * Creates a new exception with a message and a cause.
	 * @param cause
	 */
	public ConstantNotFoundException(String message, Throwable cause){
		super(message, cause);
	}
}