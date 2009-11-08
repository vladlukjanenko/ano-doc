package net.anotheria.anodoc.data;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Vector;

import net.anotheria.util.xml.XMLAttribute;
import net.anotheria.util.xml.XMLNode;


/**
 * DocumentList represents a list holder for documents. The difference between holding the 
 * documents in another document to holding the documents in a list is:<br>
 * <ol>
 * <li>Documents in the list are ordered (and keeping this order).</li>
 * <li>A DocumentList can contain multiple Documents with same name, a Document can't.</li>
 * <li>Documents in the list can be accessed by name and position</li> 
 * </ol>
 * @since 1.0
 **/
public class DocumentList<D extends Document> extends DataHolder implements ICompositeDataObject{
	/**
	 * svid.
	 */
	private static final long serialVersionUID = -6201606233792279067L;
	
	/**
	 * Internal list used for storage of the documents.
	 */
	private ArrayList<D> list;
	
	/**
	 * Creates a new list with given name.
	 */
	public DocumentList(String name){
		super(name);
		list = new ArrayList<D>();
	}
	
	/**
	 * Returns the document at the given position in the list.
	 */
	public D getDocumentAt(int pos){
		return list.get(pos);
	}
	
	/**
	 * Adds given document to list (at last position).
	 */
	public void addDocument(D doc){
		list.add(doc);
	}
	
	/**
	 * Returns the underlying list. Don't use it, if you don't know
	 * for sure.
	 */
	public List<D> getList(){
		return list;
	}
	
	/**
	 * Removes given document from the list.
	 * <em>warning: could make problems in distributed environment,
	 * unless equals method of the document is distribution save</em>.
	 */
	public void removeDocument(D doc){
		list.remove(doc);
	}	

	/**
	 * Removes document with given position from the list.
	 */
	public void removeDocumentAt(int pos){
		list.remove(pos);
	}	
	
	/**
	 * Returns true if the list contains a document with name equal to 
	 * parameter name. Otherwise returns false.
	 */
	public boolean containsDocument(String id){
		for (Document d: list){
			if (d.getId().equals(id))
				return true;
		}
		return false;
	}
	
	/**
	 * Returns document with given name, if it exists in the list.
	 * Otherwise throws a NoSuchDocumentException.
	 * @throws NoSuchDocumentException.
	 */
	public D getDocumentById(String id){
		for (int i=0; i<list.size(); i++){
			D doc = list.get(i);
			if (doc.getId().equals(id))
				return doc;
		}
		throw new NoSuchDocumentException(id);
	}
	
	/**
	 * Inserts given into the list at position with parameter 'pos'.
	 */
	public void insertDocumentAt(D doc, int pos){
		list.add(pos, doc);
	}
	
	/**
	 * Removes document with parameter name from the list, if such
	 * a document exists in the list. Otherwise does nothing.
	 */
	public void removeDocumentById(String id){
		for (int i=0; i<list.size(); i++){
			D doc = list.get(i);
			if (doc.getId().equals(id)){
				list.remove(i);
				break;
			}
		}
	}
	
	/**
	 * Returns the keys of the contained documents.
	 * @see net.anotheria.anodoc.data.ICompositeDataObject#getKeys()
	 */
	public Enumeration<String> getKeys() {
		Vector<String> keys = new Vector<String>(list.size());
		for (int i=0; i<list.size(); i++){
			IBasicStoreableObject obj = list.get(i);
			String key = obj.getStorageId();
			key = ""+i+IHelperConstants.DELIMITER+key;
			keys.add(key);
		}
		return keys.elements();
		
	}

	/**
	 * Returns the Document contained in this list under the given key. 
	 * <br><b>Note: </b>a DocumentList can contain more then one entry with 
	 * a given name, therefore a name of the document isn't unique in the list,
	 * but the key is.<br>
	 * @see net.anotheria.anodoc.data.ICompositeDataObject#getObject(String)
	 */
	public D getObject(String key) {
		String intKey = key.substring(0,key.indexOf(IHelperConstants.DELIMITER));
		return list.get(Integer.parseInt(intKey));
	}

	/**
	 * Returns the storage id of this list.
	 * @see net.anotheria.anodoc.data.IBasicStoreableObject#getStorageId()
	 */
	@Override public String getStorageId() {
		return IHelperConstants.IDENTIFIER_LIST+
				IHelperConstants.DELIMITER+
				getId();
	}
	
	/**
	 * Returns the String representation of this list. 
	 */
	@Override public String toString(){
		return "LST "+getId()+": "+list;
	}
	
	/**
	 * Returns the number of elements in this list. 
	 */
	public int getElementCount(){
		return list.size();
	}
		
	/**
	 * Returns the cumulative size of contained documents.
	 * @see net.anotheria.anodoc.data.DataHolder#getSizeInBytes()
	 */
	@Override public long getSizeInBytes() {
		int sum = 0;
		if (list==null)
			return sum;
		for (int i=0; i<list.size(); i++)
			sum += ((DataHolder)list.get(i)).getSizeInBytes();
		return sum;
	}
	
	@Override public XMLNode toXMLNode(){
		XMLNode root = new XMLNode("documentlist");
		
		root.addAttribute(new XMLAttribute("listId", getId()));
		for (D d : list){
			root.addChildNode(d.toXMLNode());
		}
		return root;
	}

}
