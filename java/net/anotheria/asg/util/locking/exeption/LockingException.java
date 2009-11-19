package net.anotheria.asg.util.locking.exeption;

/**
 * Exception for locking functionality. Shoud be used insteed of RuntimeException.
 *
 * @author: h3llka
 */
public class LockingException extends RuntimeException {

	/**
	 * Basic serialVersionUID variable.
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * Constructor.
	 *
	 * @param message message
	 */
	public LockingException(String message) {
		super(message);
	}

	/**
	 * Constructor.
	 *
	 * @param message message
	 * @param cause   exception
	 */
	public LockingException(String message, Exception cause) {
		super(message, cause);
	}

}
