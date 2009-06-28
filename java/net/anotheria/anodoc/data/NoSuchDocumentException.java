package net.anotheria.anodoc.data;

/**
 * This exception will be thrown when a not existing Document was requested,
 * or the DataHolder with correspoding name is not a Document.
 */
public class NoSuchDocumentException extends RuntimeException{
	public NoSuchDocumentException(String name){
		super("No such document "+name+", or "+name+" is not a document");
	}

}
