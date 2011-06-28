package net.anotheria.asg.exception;

/**
 * Exception thrown by getConstantByXXX methods of generated Enum types if no enum's
 * constant with such property found.
 */
public class ConstantNotFoundException extends ASGRuntimeException{
	/**
	 * Serial version ID.
	 */
	private static final long serialVersionUID = 5536478059904917632L;
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
