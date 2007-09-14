package net.anotheria.anodoc.util;

import net.anotheria.anodoc.data.IHelperConstants;

/**
 * Some utilities for key handling.
 * @since 1.0
 */
public class KeyUtility {
	/**
	 * Extracts the name of the document type (document or list) from the key.
	 */
	public static String getDocumentType(String key){
		return key.substring(0, getIndex(key));
	}	
	
	/**
	 * Extracts the name of the document from the key.
	 */
	public static String getDocumentName(String key){
		return key.substring(getIndex(key)+1);	
	}
	
	/**
	 * Returns the index of the delimiter in given string.
	 */
	private static int getIndex(String key){
		return key.indexOf(IHelperConstants.DELIMITER);
	}
	
	/**
	 * Returns true if the given key represents a document (contains document identifier in the key).
	 */
	public static boolean isDocument(String key){
		return getDocumentType(key).equals(IHelperConstants.IDENTIFIER_DOCUMENT);
	}

	/**
	 * Returns true if the given key represents a list (contains list identifier in the key).
	 */
	public static boolean isList(String key){
		return getDocumentType(key).equals(IHelperConstants.IDENTIFIER_LIST);
	}
	
	/**
	 * Returns the position of the current document from the listkey.
	 */
	public static String getListPos(String listKey){
		return listKey.substring(0, getIndex(listKey));
	}
	
	/**
	 * Extracts the document key from the listkey. A listkey is 
	 * a documentkey combined with the list position.
	 */
	public static String getKeyFromListKey(String listKey){
		return listKey.substring(getIndex(listKey)+1);	
	}
}
 