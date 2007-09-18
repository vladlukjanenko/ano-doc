package net.anotheria.anodoc.data;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;

import org.jdom.Element;

/**
 * This class represents a basic document, which is a container for properties and therefore a 
 * corresponding modell object to a simple class (with attributes). 
 * @since 1.0
 */
public class Document extends DataHolder 
		implements ICompositeDataObject, Cloneable{
	
	private static final long serialVersionUID = -5433016437476873070L;
	
	/**
	 * The internal data storage. The data storage will be parsed by a Storage Service provided with anodoc. The 
	 * Document itself will not be saved, but the data it contains.
	 */
	private Hashtable<String,DataHolder> dataStorage;
	
	/**
	 * Constant used to save type_identifier as internal property name.
	 */
	public static final String PROP_TYPE_IDENTIFIER = "###my_type###";
	
	/**
	 * Creates a new Document with given name. 
	 */
	public Document(String id){
		super(id);
		dataStorage = new Hashtable<String,DataHolder>();
		putProperty(new StringProperty(IHelperConstants.IDENTIFIER_KEY, IHelperConstants.IDENTIFIER_DOCUMENT));
	}
	
	public Document(Document anotherDocument){
		super("");
		dataStorage = new Hashtable<String,DataHolder>();
		Hashtable srcTable = anotherDocument.dataStorage;
		Enumeration src = srcTable.elements();
		while(src.hasMoreElements()){
			Property p = (Property)src.nextElement();
			try{
				putProperty((Property)p.clone());
			}catch(CloneNotSupportedException e){
				//sdhouldn't happen
				e.printStackTrace();
				throw new RuntimeException("Clone failed: "+e.getMessage());
			}
		}
		
	}

	/**
     * Returns the DataHolder contained in this Document under the given name.
     * A document can contain properties, documents and lists.
     * @see net.anotheria.anodoc.data.NoSuchDataHolderException 
	 */	
	public DataHolder getDataHolder(String name) throws NoSuchDataHolderException{
		DataHolder holder = dataStorage.get(name);
		if (holder==null)
			throw new NoSuchDataHolderException(name);
		return holder;
	} 
	
	/**
	 * Returns the Document contained in this Document under the given name. 
     * @see net.anotheria.anodoc.data.NoSuchDocumentException 
	 */
	public Document getDocument(String name) throws NoSuchDocumentException{
		try{
			DataHolder holder = getDataHolder(name);
			if (holder instanceof Document)
				return (Document)holder;
		}catch(NoSuchDataHolderException e){}
		throw new NoSuchDocumentException(name);
	}
	
	public List<Property> getProperties(){
		Collection<DataHolder> holders = dataStorage.values();
		List<Property> ret = new ArrayList<Property>();
		for (Iterator<DataHolder> it = holders.iterator(); it.hasNext(); ){
			DataHolder h = it.next();
			if (h instanceof Property){
				ret.add((Property)h);
			}
			
		}
		return ret;
	}
	
	/**
	 * Returns the DocumentList contained in this Document under the given name. 
	 * @see net.anotheria.anodoc.data.NoSuchDocumentListException 
	 */
	public DocumentList getDocumentList(String name) throws NoSuchDocumentListException{
		try{
			DataHolder holder = getDataHolder(name);
			if (holder instanceof DocumentList)
				return (DocumentList)holder;
		}catch(NoSuchDataHolderException e){}
		throw new NoSuchDocumentListException(name);
	}
	
	/**
	 * Returns the Property contained in this Document under the given name. 
	 */
	public Property getProperty(String name) throws NoSuchPropertyException{
		try{
			DataHolder holder = getDataHolder(name);
			if (holder instanceof Property)
				return (Property)holder;
		}catch(NoSuchDataHolderException e){}
		throw new NoSuchPropertyException(name,"Property");
	}
	
	/**
	 * Returns the IntProperty contained in this Document under the given name. 
	 */
	public IntProperty getIntProperty(String name) throws NoSuchPropertyException{
		try{
			Property aProperty = getProperty(name);
			if (aProperty instanceof IntProperty)
				return (IntProperty) aProperty;
		}catch(NoSuchDataHolderException e){}
		throw new NoSuchPropertyException(name, "IntProperty");
	}

	/**
	 * Returns the LongProperty contained in this Document under the given name. 
	 */
	public LongProperty getLongProperty(String name) throws NoSuchPropertyException{
		try{
			Property aProperty = getProperty(name);
			if (aProperty instanceof LongProperty)
				return (LongProperty) aProperty;
		}catch(NoSuchDataHolderException e){}
		throw new NoSuchPropertyException(name,"LongProperty");
	}
	
	/**
	 * Returns the StringProperty contained in this Document under the given name. 
	 */
	public StringProperty getStringProperty(String name) throws NoSuchPropertyException{
		try{
			Property aProperty = getProperty(name);
			if (aProperty instanceof StringProperty)
				return (StringProperty) aProperty;
		}catch(NoSuchDataHolderException e){}
		throw new NoSuchPropertyException(name, "StringProperty");
	}

	/**
	 * Returns the BooleanProperty contained in this Document under the given name. 
	 */
	public BooleanProperty getBooleanProperty(String name) throws NoSuchPropertyException{
		try{
			Property aProperty = getProperty(name);
			if (aProperty instanceof BooleanProperty)
				return (BooleanProperty) aProperty;
		}catch(NoSuchDataHolderException e){}
		throw new NoSuchPropertyException(name,"BooleanProperty");
	}

	/**
	 * Returns the ListProperty contained in this Document under the given name. 
	 */
	public ListProperty getListProperty(String name) throws NoSuchPropertyException{
		try{
			Property aProperty = getProperty(name);
			if (aProperty instanceof ListProperty)
				return (ListProperty) aProperty;
		}catch(NoSuchDataHolderException e){}
		throw new NoSuchPropertyException(name, "ListProperty");
	}
	
	/**
	 * Returns the FloatProperty contained in this Document under the given name. 
	 */
	public FloatProperty getFloatProperty(String name) throws NoSuchPropertyException{
		try{
			Property aProperty = getProperty(name);
			if (aProperty instanceof FloatProperty)
				return (FloatProperty) aProperty;
		}catch(NoSuchDataHolderException e){}
		throw new NoSuchPropertyException(name, "FloatProperty");
	}
	
	/**
	 * Returns the DoubleProperty contained in this Document under the given name. 
	 */
	public DoubleProperty getDoubleProperty(String name) throws NoSuchPropertyException{
		try{
			Property aProperty = getProperty(name);
			if (aProperty instanceof DoubleProperty)
				return (DoubleProperty) aProperty;
		}catch(NoSuchDataHolderException e){}
		throw new NoSuchPropertyException(name, "DoubleProperty");
	}

	/**
	 * Returns the DocumentList contained in this Document under the given name in any case,
	 * which means that if no DocumentList is contained it a new will be created.  
	 * This function is protected because it implies very much knowledge about the module 
	 * structure and shouldn't be called from outside a document.
	 * @see net.anotheria.anodoc.data.NoSuchDocumentListException 
	 */
	protected DocumentList getDocumentListAnyCase(String name) {
		try{
			return getDocumentList(name);
		}catch(NoSuchDocumentListException e){};
		DocumentList newList = createDocumentList(name);
		putList(newList);
		return newList;
	}
	
	/**
	 * Returns the Document contained in this Document under the given name in any case,
	 * which means that if no Document is contained it a new will be created. 
	 * This function is protected because it implies very much knowledge about the module 
	 * structure and shouldn't be called from outside a document.
	 * @see net.anotheria.anodoc.data.NoSuchDocumentException 
	 */
	protected Document getDocumentAnyCase(String name) {
		try{
			return getDocument(name);
		}catch(NoSuchDocumentException e){};
		Document newDoc = createDocument(name);
		putDocument(newDoc);
		return newDoc;
	}
	
	protected ListProperty getListPropertyAnyCase(String name){
		 try{
		 	return getListProperty(name);
		 }catch(NoSuchPropertyException e){
		 	System.out.println(e);
		 }
		 ListProperty list = new ListProperty(name);
		 putProperty(list);
		 return list;
	}
	
	/**
	 * Creates a new DocumentList. Overwrite this, if your document
	 * uses special lists.
	 * Called by getDocumentListAnyCase
	 * @param name
	 * @return
	 */
	protected DocumentList createDocumentList(String name){
		return new DocumentList(name);
	}
	
	/**
	 * Creates a new Document. Overwrite this, if your document
	 * uses special subdocuments (which should almost ever be the case).
	 * Called by getDocumentAnyCase
	 * @param name
	 * @return
	 */
	protected Document createDocument(String name){
		return new Document(name);
	}
	
	protected ListProperty createListProperty(String name){
		return new ListProperty(name);
	}

	protected void removeDataHolder(DataHolder holder){
		dataStorage.remove(holder.getId());
	}
	
	protected void removeDataHolder(String id){
		dataStorage.remove(id);
	}
	
	/**
	 * Puts the given DataHolder (which can be a document, a list or a property)
	 * in the internal storage.
	 */
	protected void addDataHolder(DataHolder holder){
		dataStorage.put(holder.getId(), holder);
	}
	
	/**
	 * Puts the given StringProperty in the internal storage.
	 */
	public void putStringProperty(StringProperty p){
		putProperty(p);
	}
	
	/**
	 * Puts the given IntProperty in the internal storage.
	 */
	public void putIntProperty(IntProperty p){
		putProperty(p);
	}

	/**
	 * Puts the given LongProperty in the internal storage.
	 */
	public void putLongProperty(LongProperty p){
		putProperty(p);
	}

	/**
	 * Puts the given BooleanProperty in the internal storage.
	 */
	public void putBooleanProperty(BooleanProperty p){
		putProperty(p);
	}

	/**
	 * Puts the given ListProperty in the internal storage.
	 */
	public void putListProperty(ListProperty p){
		putProperty(p);
	}
	
	/**
	 * Puts the given FloatProperty in the internal storage.
	 */
	public void putFloatProperty(FloatProperty p){
		putProperty(p);
	}
	
	/**
	 * Puts the given FloatProperty in the internal storage.
	 */
	public void putDoubleProperty(DoubleProperty p){
		putProperty(p);
	}

	/**
	 * Puts the given Property in the internal storage.
	 */
	public void putProperty(Property p){
		addDataHolder(p);
	}
	
	public void removeProperty(Property p){
		removeDataHolder(p);
	}
	
	public void removeProperty(String propertyName){
		removeDataHolder(propertyName);
	}

	/**
	 * Puts the given Document in the internal storage.
	 */
	public void putDocument(Document doc){
		addDataHolder(doc);
	}
	
	/**
	 * Puts the given DocumentList in the internal storage.
	 */
	public void putList(DocumentList list){
		addDataHolder(list);
	}

	/**
	 * Returns the string representation of this document.
	 */
	public String toString(){
		return "DOC "+getId()+" "+dataStorage;
	}
	
	///////////// STORAGE /////////////
	
	
	/**
	 * Returns the keys (names) of the contained documents.
	 * @see net.anotheria.anodoc.data.ICompositeDataObject#getKeys()
	 */
	public Enumeration<String> getKeys() {
		return dataStorage.keys();
	}

	/**
	 * Returns the contained object stored under given key.
	 * @see net.anotheria.anodoc.data.ICompositeDataObject#getObject(String)
	 */
	public Object getObject(String key) {
		return dataStorage.get(key);
	}

	/**
	 * Returns the storage id which should be used by a storage to 
	 * store this document.
	 * @see net.anotheria.anodoc.data.IBasicStoreableObject#getStorageId()
	 */
	public String getStorageId() {
		return IHelperConstants.IDENTIFIER_DOCUMENT+
				IHelperConstants.DELIMITER+
				getId();
	} 
	
	/////////////////// TYPE IDENTIFIER FOR DATENBANK und FACTORY
	/**
	 * Returns the type identifier of this document.
	 */
	public String getTypeIdentifier(){
		return getString(PROP_TYPE_IDENTIFIER);
	}

	/**
	 * Sets the type identifier for this document. The type identifier is 
	 * especially useful for queries and factory-reassembling.
	 * @param anIdentifier
	 */	
	public void setTypeIdentifier(String anIdentifier){
		setString(PROP_TYPE_IDENTIFIER, anIdentifier);
	}
	
	//// WEITERE NUETZLICHE FUNKTION ////
	/**
	 * Returns the string value of the according StringProperty,
	 * or empty string (see getEmptyString) if none set.
	 */
	public String getString(String fieldId) throws NoSuchPropertyException{
		try{
			return getStringProperty(fieldId).getString();
		}catch(NoSuchPropertyException e){
			return getEmptyString();
		}
	}
	
	/**
	 * Returns the long value of the according LongProperty,
	 * or an empty long (see getEmptyLong) if none set.
	 */
	public long getLong(String fieldId) throws NoSuchPropertyException{
		try{
			return getLongProperty(fieldId).getlong();
		}catch(NoSuchPropertyException e){
			return getEmptyLong();
		}
	}
	
	/**
	 * Returns the int value of the according IntProperty,
	 * or an empty int (see getEmptyInt) if none set.
	 */
	public int getInt(String fieldId) throws NoSuchPropertyException{
		try{
			return getIntProperty(fieldId).getInt();
		}catch(NoSuchPropertyException e){
			return getEmptyInt();
		}
	}
	
	public List getList(String fieldId) throws NoSuchPropertyException{
		try{
			return getListProperty(fieldId).getList();
		}catch(NoSuchPropertyException e){
			return getEmptyList();
		}
	}

	/**
	 * Returns the float value of the according FloatProperty,
	 * or an empty float (see getEmptyFloat) if none set.
	 */
	public float getFloat(String fieldId) throws NoSuchPropertyException{
		try{
			return getFloatProperty(fieldId).getfloat();
		}catch(NoSuchPropertyException e){
			return getEmptyFloat();
		}
	}

	/**
	 * Returns the double value of the according DoubleProperty,
	 * or an empty double (see getEmptyDouble) if none set.
	 */
	public double getDouble(String fieldId) throws NoSuchPropertyException{
		try{
			return getDoubleProperty(fieldId).getdouble();
		}catch(NoSuchPropertyException e){
			return getEmptyDouble();
		}
	}

	/**
	 * Returns the boolean value of the according BooleanProperty,
	 * or an empty boolean (see getEmptyBoolean) if none set.
	 */
	public boolean getBoolean(String fieldId) throws NoSuchPropertyException{
		try{
			return getBooleanProperty(fieldId).getboolean();
		}catch(NoSuchPropertyException e){
			return getEmptyBoolean();
		}
	}
	
	public void setList(String fieldId, List value){
		putListProperty(new ListProperty(fieldId, value));
	}

	/**
	 * Sets a StringProperty with name = fieldId and value = value in this document.
	 */
	public void setString(String fieldId, String value){
		putStringProperty(new StringProperty(fieldId, value));
	}
	
	/**
	 * Sets a LongProperty with name = fieldId and value = value in this document.
	 */
	public void setLong(String fieldId, long value){
		putLongProperty(new LongProperty(fieldId, value));
	}
	
	/**
	 * Sets a IntProperty with name = fieldId and value = value in this document.
	 */
	public void setInt(String fieldId, int value){
		putIntProperty(new IntProperty(fieldId, value));
	}
	
	/**
	 * Sets a FloatProperty with name = fieldId and value = value in this document.
	 */
	public void setFloat(String fieldId, float value){
		putFloatProperty(new FloatProperty(fieldId, value));
	}

	/**
	 * Sets a FloatProperty with name = fieldId and value = value in this document.
	 */
	public void setDouble(String fieldId, double value){
		putDoubleProperty(new DoubleProperty(fieldId, value));
	}

	/**
	 * Sets a BooleangProperty with name = fieldId and value = value in this document.
	 */
	public void setBoolean(String fieldId, boolean value){
		putBooleanProperty(new BooleanProperty(fieldId, value));
	}
	
	/**
	 * Returns the initial value for a string (empty string - ""). Overwrite it if you wish another values. An empty value will always be returned if  
	 * you call get'Type' and the corresponding property doesn't exists.   
	 */
	 
	public String getEmptyString(){
		return "";
	}
	
	/**
	 * Returns the initial value for a long (0). Overwrite it if you wish another values. An empty value will always be returned if  
	 * you call get'Type' and the corresponding property doesn't exists.   
	 */
	public long getEmptyLong(){
		return 0;
	}
	
	/**
	 * Returns the initial value for a int (0). Overwrite it if you wish another values. An empty value will always be returned if  
	 * you call get'Type' and the corresponding property doesn't exists.   
	 */
	public int getEmptyInt(){
		return 0;
	}
	
	/**
	 * Returns the initial value for a float (0.0). Overwrite it if you wish another values. An empty value will always be returned if  
	 * you call get'Type' and the corresponding property doesn't exists.   
	 */
	public float getEmptyFloat(){
		return 0;
	}

	/**
	 * Returns the initial value for a double (0.0). Overwrite it if you wish another values. An empty value will always be returned if  
	 * you call get'Type' and the corresponding property doesn't exists.   
	 */
	public double getEmptyDouble(){
		return 0.0;
	}

	/**
	 * Returns the initial value for a boolean (false). Overwrite it if you wish another values. An empty value will always be returned if  
	 * you call get'Type' and the corresponding property doesn't exists.   
	 */
	public boolean getEmptyBoolean(){
		return false;
	}
	
	public List getEmptyList(){
		return new ArrayList();
	}
	
	/**
	 * Returns the cumulative size of the contained DataHolders.
	 * @see net.anotheria.anodoc.data.DataHolder#getSizeInBytes()
	 */
	public long getSizeInBytes() {
		int sum = 0;
		Enumeration values = dataStorage.elements();
		while(values.hasMoreElements()){
			sum += ((DataHolder)values.nextElement()).getSizeInBytes();
		}
		return sum;
	}

	public void renameTo(String newId){
		setId(newId);
	}
	
	@SuppressWarnings("unchecked")
	public Object clone() throws CloneNotSupportedException{
		Object ret = super.clone();
		((Document)ret).dataStorage = (Hashtable<String,DataHolder>)dataStorage.clone();
		return ret;
	}

	public Element toXMLElement(){
		Element root = new Element("document");
		
		root.setAttribute("documentId", getId());
		List<Element> childs = new ArrayList<Element>();
		for (Iterator<DataHolder> it = dataStorage.values().iterator(); it.hasNext();)
			childs.add(it.next().toXMLElement());
		
		
		root.setChildren(childs);
		return root;
	}
	
	public Object getPropertyValue(String propertyName){
		return getProperty(propertyName).getValue();
	}
}