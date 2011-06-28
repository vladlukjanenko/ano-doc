package net.anotheria.anodoc.data;

/**
 * A class which implements this interface understands itself as basic (plain, atomic) 
 * storeable object, which means, that it doesn't contain futher storeable objects. <br>
 * Therefore it's suitable for properties and other simple things, but not for whole 
 * documents.
 * @see net.anotheria.anodoc.data.ICompositeDataObject
 * @see net.anotheria.anodoc.data.IPlainDataObject
 * @since 1.0  
 */
public interface IBasicStoreableObject {
	/**
	 * Returns the id which is used to store this object.
	 * This id should be an unique identifier 
	 * in the objects environment (container).
	 * @return id which is used to store this object
	 */
	String getStorageId();
}
