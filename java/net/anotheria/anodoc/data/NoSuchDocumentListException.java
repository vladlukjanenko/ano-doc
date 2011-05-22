package net.anotheria.anodoc.data;

/**
 * This exception will be thrown when a not existing DocumentList was requested,
 * or the DataHolder with correspoding name is not a DocumentList.
 */
public class NoSuchDocumentListException extends RuntimeException{
	/**
	 * SerialVersionUID.
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * Creates a new exception instance.
	 * @param name
	 */
	public NoSuchDocumentListException(String name){
		super("No such list "+name+", or "+name+" is not a list");
	}

}
