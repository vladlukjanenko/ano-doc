package net.anotheria.anodoc.data;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;

import net.anotheria.anodoc.util.context.CallContext;
import net.anotheria.anodoc.util.context.ContextManager;
import net.anotheria.util.xml.XMLAttribute;
import net.anotheria.util.xml.XMLNode;

/**
 * This class represents a basic document, which is a container for properties and therefore a 
 * corresponding modell object to a simple class (with attributes). 
 * @since 1.0
 * @author lrosenberg
 */
public class Document extends DataHolder 
		implements ICompositeDataObject, Cloneable{
	
	/**
	 * svid.
	 */
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
	 * Constant for property name of the property under which last update timestamp is stored.
	 */
	public static final String PROP_LAST_UPDATE = "###last_update###";
	
	/**
	 * Constant for property name of the property under which Author is stored.
	 */
	public static final String PROP_AUTHOR = "###author###";

	/**
	 * Creates a new Document with given name. 
	 */
	public Document(String id){
		super(id);
		dataStorage = new Hashtable<String,DataHolder>();
		putProperty(new StringProperty(IHelperConstants.IDENTIFIER_KEY, IHelperConstants.IDENTIFIER_DOCUMENT));
	}
	
	/**
	 * Creates a new document as a copy of another document.
	 * @param anotherDocument the document to be copied
	 */
	public Document(Document anotherDocument){
		super("");
		dataStorage = new Hashtable<String,DataHolder>();
		Hashtable<String,DataHolder> srcTable = anotherDocument.dataStorage;
		Enumeration<DataHolder> src = srcTable.elements();
		while(src.hasMoreElements()){
			Property p = (Property)src.nextElement();
			try{
				putProperty((Property)p.clone());
			}catch(CloneNotSupportedException e){
				throw new RuntimeException("Clone failed: "+e.getMessage());
			}
		}
		
	}

	/**
     * Returns the DataHolder contained in this Document under the given name.
     * A document can contain properties, documents and lists.
     * @see net.anotheria.anodoc.data.NoSuchDataHolderException
	 * @param name of DataHolder
	 * @return DataHolder
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
	 * @param name of Document
	 * @return Document
	 */
	public Document getDocument(String name) throws NoSuchDocumentException{
		try{
			DataHolder holder = getDataHolder(name);
			if (holder instanceof Document)
				return (Document)holder;
		}catch(NoSuchDataHolderException e){}
		throw new NoSuchDocumentException(name);
	}
	
	/**
	 * Returns a list of all contained properties.
	 * @return properties
	 */
	public List<Property> getProperties(){
		Collection<DataHolder> holders = dataStorage.values();
		List<Property> ret = new ArrayList<Property>();
		Iterator<DataHolder> it = holders.iterator();
		while(it.hasNext()){
			DataHolder h = it.next();
			if (h instanceof Property){
				ret.add((Property)h);
			}
		}
		return ret;
	}
	
	/**
	 * Returns the DocumentList contained in this Document under the given name. 
	 * @param name of DocumentList
	 * @throws net.anotheria.anodoc.data.NoSuchDocumentListException
	 * @return DocumentList
	 */
	@SuppressWarnings("unchecked")
	public <D extends Document> DocumentList<D> getDocumentList(String name){
		try{
			DataHolder holder = getDataHolder(name);
			if (holder instanceof DocumentList)
				return (DocumentList<D>)holder;
		}catch(NoSuchDataHolderException e){}
		throw new NoSuchDocumentListException(name);
	}
	
	/**
	 * Returns the Property contained in this Document under the given name.
	 * @param name of property
	 * @return Property
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
	 * @param name of property
	 * @return IntProperty
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
	 * @param name of property
	 * @return LongProperty
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
	 * @param name of StringProperty
	 * @return StringProperty
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
	 * @param name of BooleanProperty
	 * @return BooleanProperty
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
	 * @param name of ListProperty
	 * @return ListProperty
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
	 * @param name of FloatProperty
	 * @return FloatProperty
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
	 * @param name of DoubleProperty
	 * @return DoubleProperty
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
	 * @param name of Document
	 * @return DocumentList
	 */
	protected <D extends Document> DocumentList<D> getDocumentListAnyCase(String name) {
		try{
			return getDocumentList(name);
		}catch(NoSuchDocumentListException e){
			
		}
		DocumentList<D> newList = createDocumentList(name);
		putList(newList);
		return newList;
	}
	
	/**
	 * Returns the Document contained in this Document under the given name in any case,
	 * which means that if no Document is contained it a new will be created. 
	 * This function is protected because it implies very much knowledge about the module 
	 * structure and shouldn't be called from outside a document.
	 * @see net.anotheria.anodoc.data.NoSuchDocumentException
	 * @param name of document
	 * @return Doucument
	 */
	protected Document getDocumentAnyCase(String name) {
		try{
			return getDocument(name);
		}catch(NoSuchDocumentException e){}
		Document newDoc = createDocument(name);
		putDocument(newDoc);
		return newDoc;
	}
	
	protected ListProperty getListPropertyAnyCase(String name){
		 try{
		 	return getListProperty(name);
		 }catch(NoSuchPropertyException e){}
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
	protected <D extends Document> DocumentList<D> createDocumentList(String name){
		return new DocumentList<D>(name);
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
	public void putList(DocumentList<? extends Document> list){
		addDataHolder(list);
	}

	/**
	 * Returns the string representation of this document.
	 */
	@Override public String toString(){
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
	 * @return long value
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
	 * @return int value
	 */
	public int getInt(String fieldId) throws NoSuchPropertyException{
		try{
			return getIntProperty(fieldId).getInt();
		}catch(NoSuchPropertyException e){
			return getEmptyInt();
		}
	}

	/**
	 * Returns list of Property by fieldId.
	 * @param fieldId fieldId
	 * @return list of Property or empty list if no Property was found by diven fieldId
	 * @throws NoSuchPropertyException
	 */
	public List<Property> getList(String fieldId) throws NoSuchPropertyException{
		try{
			return getListProperty(fieldId).getList();
		}catch(NoSuchPropertyException e){
			return getEmptyList();
		}
	}

	/**
	 * Returns the float value of the according FloatProperty,
	 * or an empty float (see getEmptyFloat) if none set.
	 * @return float value of FloatProperty
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
	 * @return double value of DoubleProperty
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
	 * @return boolean value
	 */
	public boolean getBoolean(String fieldId) throws NoSuchPropertyException{
		try{
			return getBooleanProperty(fieldId).getboolean();
		}catch(NoSuchPropertyException e){
			return getEmptyBoolean();
		}
	}
	
	public void setList(String fieldId, List<Property> value){
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
	 *
	 * @return ""
	 */
	public String getEmptyString() {
		return "";
	}

	/**
	 * Returns the initial value for a long (0). Overwrite it if you wish another values. An empty value will always be returned if  
	 * you call get'Type' and the corresponding property doesn't exists.   
	 * @return 0
	 */
	public long getEmptyLong(){
		return 0;
	}
	
	/**
	 * Returns the initial value for a int (0). Overwrite it if you wish another values. An empty value will always be returned if  
	 * you call get'Type' and the corresponding property doesn't exists.   
	 * @return 0
	 */
	public int getEmptyInt(){
		return 0;
	}
	
	/**
	 * Returns the initial value for a float (0.0). Overwrite it if you wish another values. An empty value will always be returned if  
	 * you call get'Type' and the corresponding property doesn't exists.   
	 * @return 0
	 */
	public float getEmptyFloat(){
		return 0;
	}

	/**
	 * Returns the initial value for a double (0.0). Overwrite it if you wish another values. An empty value will always be returned if  
	 * you call get'Type' and the corresponding property doesn't exists.   
	 * @return 0.0
	 */
	public double getEmptyDouble(){
		return 0.0;
	}

	/**
	 * Returns the initial value for a boolean (false). Overwrite it if you wish another values. An empty value will always be returned if  
	 * you call get'Type' and the corresponding property doesn't exists.   
	 * @return false
	 */
	public boolean getEmptyBoolean(){
		return false;
	}
	
	public List<Property> getEmptyList(){
		return new ArrayList<Property>();
	}
	
	/**
	 * Returns the cumulative size of the contained DataHolders.
	 * @see net.anotheria.anodoc.data.DataHolder#getSizeInBytes()
	 * @return size of the contained DataHolders
	 */
	public long getSizeInBytes() {
		int sum = 0;
		Enumeration<DataHolder> values = dataStorage.elements();
		while(values.hasMoreElements()){
			sum += values.nextElement().getSizeInBytes();
		}
		return sum;
	}

	public void renameTo(String newId){
		setId(newId);
	}
	
	@SuppressWarnings("unchecked")
	@Override public Object clone() throws CloneNotSupportedException{
		Object ret = super.clone();
		((Document)ret).dataStorage = (Hashtable<String,DataHolder>)dataStorage.clone();
		return ret;
	}

	@Override public XMLNode toXMLNode(){
		XMLNode root = new XMLNode("document");
		
		root.addAttribute(new XMLAttribute("documentId", getId()));
		for (Iterator<DataHolder> it = dataStorage.values().iterator(); it.hasNext();)
			root.addChildNode(it.next().toXMLNode());
		
		return root;
	}
	
	public Object getPropertyValue(String propertyName){
		return getProperty(propertyName).getValue();
	}
	
	public void setLastUpdateNow(){
		setLong(PROP_LAST_UPDATE, System.currentTimeMillis());
	}
	
	public long getLastUpdateTimestamp(){
		return getLong(PROP_LAST_UPDATE); 
	}
	
	public void setCallContextAuthor(){
		CallContext callContext = ContextManager.getCallContext();
		String author = callContext != null? callContext.getCurrentAuthor(): "UNKNOWN";
		setString(PROP_AUTHOR, author);
	}
	
	public String getAuthor(){
		return getString(PROP_AUTHOR); 
	}
	
	protected List<String> copyToStringList(List<Property> properties){
		ArrayList<String> ret = new ArrayList<String>(properties.size());
		for (Property p : properties)
			ret.add(((StringProperty)p).getString());
		return ret;
	}
	
	protected List<Property> copyFromStringList(List<String> strings){
		ArrayList<Property> ret = new ArrayList<Property>();
		for (String s : strings)
			ret.add(new StringProperty(s,s));
		return ret;
	}
	
	protected  List<Integer> copyToIntegerList(List<Property> properties){
		ArrayList<Integer> ret = new ArrayList<Integer>(properties.size());
		for (Property p : properties)
			ret.add(((IntProperty)p).getInt());
		return ret;
	}
	
	protected List<Property> copyFromIntegerList(List<Integer> integers){
		ArrayList<Property> ret = new ArrayList<Property>();
		for (int i : integers)
			ret.add(new IntProperty(""+ i,i));
		return ret;
	}
	
	protected  List<Long> copyToLongList(List<Property> properties){
		ArrayList<Long> ret = new ArrayList<Long>(properties.size());
		for (Property p : properties)
			ret.add(((LongProperty)p).getLong());
		return ret;
	}
	
	protected List<Property> copyFromLongList(List<Long> longs){
		ArrayList<Property> ret = new ArrayList<Property>();
		for (long l: longs)
			ret.add(new LongProperty(""+ l, l));
		return ret;
	}
	
	protected  List<Boolean> copyToBooleanList(List<Property> properties){
		ArrayList<Boolean> ret = new ArrayList<Boolean>(properties.size());
		for (Property p : properties)
			ret.add(((BooleanProperty)p).getBoolean());
		return ret;
	}
	
	protected List<Property> copyFromBooleanList(List<Boolean> booleans){
		ArrayList<Property> ret = new ArrayList<Property>();
		for (boolean b: booleans)
			ret.add(new BooleanProperty(""+ b, b));
		return ret;
	}
	
	protected  List<Double> copyToDoubleList(List<Property> properties){
		ArrayList<Double> ret = new ArrayList<Double>(properties.size());
		for (Property p : properties)
			ret.add(((DoubleProperty)p).getDouble());
		return ret;
	}
	
	protected List<Property> copyFromDoubleList(List<Double> doubles){
		ArrayList<Property> ret = new ArrayList<Property>();
		for (double d: doubles)
			ret.add(new DoubleProperty(""+ d, d));
		return ret;
	}
	
	protected  List<Float> copyToFloatList(List<Property> properties){
		ArrayList<Float> ret = new ArrayList<Float>(properties.size());
		for (Property p : properties)
			ret.add(((FloatProperty)p).getFloat());
		return ret;
	}
	
	protected List<Property> copyFromFloatList(List<Float> floats){
		ArrayList<Property> ret = new ArrayList<Property>();
		for (float d: floats)
			ret.add(new FloatProperty(""+ d, d));
		return ret;
	}
}
