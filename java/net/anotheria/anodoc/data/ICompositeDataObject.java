package net.anotheria.anodoc.data;

import java.util.Enumeration;

/**
 * This container represents a composite container like 
 * a Document or a DocumentList. 
 * @see net.anotheria.anodoc.data.IPlainDataObject
 * @see net.anotheria.anodoc.data.IBasicStoreableObject
 */
public interface ICompositeDataObject extends IBasicStoreableObject{
	/**
	 * This method is used by the storage to query the underlying data. It's
	 * also used by debug or administration programs to trace a composite object, such as Document.  
	 * @return the keys (ids) of contained elements.
	 */	
	Enumeration<String> getKeys();
	
	/**
	 * This method is used by the storage to save not only the container, but the underlying data too. It's
	 * also used by debug or administration programs to trace a composite object, such as Document.  
	 * @param key the key (id) of the contained element/object.
	 * @return the object associated to the key.
	 */
	Object getObject(String key);
	
}
