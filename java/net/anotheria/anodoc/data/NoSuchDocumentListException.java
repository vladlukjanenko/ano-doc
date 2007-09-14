package net.anotheria.anodoc.data;

/**
 * This exception will be thrown when a not existing BGLDocumentList was requested,
 * or the BGLDataHolder with correspoding name is not a BGLDocumentList.
 */
public class NoSuchDocumentListException extends RuntimeException{
	public NoSuchDocumentListException(String name){
		super("No such list "+name+", or "+name+" is not a list");
	}

}
