package net.anotheria.anodoc.data;

/**
 * This exception will be thrown when a not existing BGLDocument was requested,
 * or the BGLDataHolder with correspoding name is not a BGLDocument.
 */
public class NoSuchDocumentException extends RuntimeException{
	public NoSuchDocumentException(String name){
		super("No such document "+name+", or "+name+" is not a document");
	}

}
