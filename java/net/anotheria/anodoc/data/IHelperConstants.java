package net.anotheria.anodoc.data;

/**
 * Some constants used by the assembling and dissembling routines. 
 */
public interface IHelperConstants {
	/**
	 * Used as delimiter for constants which are assembled from different parts.
	 */
	public static final char DELIMITER = '$';
	/**
	 * Used as prefix for list names when stored in modules.
	 */
	public static final String IDENTIFIER_LIST = "list";
	/**
	 * Used as prefix for doc names when stored in modules.
	 */
	public static final String IDENTIFIER_DOCUMENT = "doc";
	
	/**
	 * Used as name for type of stored object, for example document.
	 */
	public static final String IDENTIFIER_KEY = "__type_identifier__";
}
