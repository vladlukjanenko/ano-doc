package net.anotheria.anodoc.data;

import java.util.Enumeration;

/**
 * This container represents a composite container like 
 * a BGLDocument or a BGLDocumentList. 
 * @see biz.beaglesoft.bgldoc.data.IPlainDataObject
 * @see biz.beaglesoft.bgldoc.data.IBasicStoreableObject
 */
public interface ICompositeDataObject extends IBasicStoreableObject{
	/**
	 * This method is used by the storage to query the underlying data. It's
	 * also used by debug or administration programs to trace a composite object, such as BGLDocument.  
	 * @return the keys (ids) of contained elements.
	 */	
	public Enumeration getKeys();
	
	/**
	 * This method is used by the storage to save not only the container, but the underlying data too. It's
	 * also used by debug or administration programs to trace a composite object, such as BGLDocument.  
	 * @param key the key (id) of the contained element/object.
	 * @return the object associated to the key.
	 */
	public Object getObject(String key);
	
}
