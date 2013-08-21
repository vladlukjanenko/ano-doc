package net.anotheria.anodoc.data;

import net.anotheria.anodoc.service.IModuleFactory;
import net.anotheria.anodoc.util.KeyUtility;
import net.anotheria.util.xml.XMLAttribute;
import net.anotheria.util.xml.XMLNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.util.Collection;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

/**
 * This class describes a Module, which is a unity that can be stored
 * and identified by the framework. Each module is identified by the 
 * tuple (moduleId, ownerId, copyId). The moduleId (or just id) is used to determine 
 * the proper factory and storage, the ownerId represents the owner of the module, which
 * could be a user of the system or a content piece by content supporting functions (comments, netlogs, etc)
 * and the copyId is used for multiple copies systems or for context separation.<br>
 * A Module shouldn't be used directly, instead it should be extended and 
 * enhanced with the methods that are needed for the business logic.  
 */
public class Module implements ICompositeDataObject, Serializable{
	/**
	 * The id of the module (moduleId).
	 */
	private String id;
	
	/**
	 * Contained holders.
	 */
	private Hashtable<String,DataHolder> holders;
	
	/**
	 * The id of the owner of this instance.
	 */
	private String ownerId;
	
	/**
	 * The copy id.
	 */
	private String copyId;
	
	/**
	 * log is transient, so it will not be transmitted via network in a 
	 * distributed environment.
	 */
	private transient Logger log;

	/**
	 * svid.
	 */
	private static final long serialVersionUID = 4896753471545492611L;

	/**
	 * Returns the current logger. Creates one if needed.  
	 */
	protected Logger getLog(){
		return log;
	}
	
	/**
	 * The factory is mainly needed for document reassembling.
	 */
	private IModuleFactory moduleFactory;
	
	/**
	 * Delimiter used to separate parts of the key.
	 */
	private static final char DELIMITER = '$';
	
	/**
	 * Creates a new Module with given moduleId.
	 * @param anId the id of the module.
	 */
	public Module(String anId){
		this.id = anId;
		holders = new Hashtable<String,DataHolder>();
		getLog();//ensuring that logger is initialized.
		log = LoggerFactory.getLogger(this.getClass());
	}
	
	/**
	 * Returns the document with given name (if there is any).
	 */
	public Document getDocument(String name) throws NoSuchDocumentException{
		DataHolder doc = getDataHolder(name);
		if (doc==null || (!(doc instanceof Document)))
			throw new NoSuchDocumentException(name);
		return (Document)doc;
	}
	
	/**
	 * Returns the documentlist with given name (if there is any).
	 */
	@SuppressWarnings("unchecked")
	public <D extends Document>DocumentList<D> getList(String name) throws NoSuchDocumentListException{
		DataHolder list = getDataHolder(name);
		if (list==null || (!(list instanceof DocumentList)))
			throw new NoSuchDocumentListException(name);
		return (DocumentList<D>)list;
	}
	
	/**
	 * Puts the given list into this module. 
	 */
	@SuppressWarnings("unchecked")
	public void putList(DocumentList aList){
		putDataHolder(aList);
	}
	
	/**
	 * Puts the given document into this module. 
	 */
	public void putDocument(Document aDoc){
		putDataHolder(aDoc);
	}
	
	/**
	 * Puts the given data holder into this module. 
	 */
	private void putDataHolder(DataHolder holder){
		holders.put(holder.getId(), holder);
	}
	
	/**
	 * Returns the data holder with given name (key). 
	 */
	private DataHolder getDataHolder(String key){
		return holders.get(key);
	}
	
		
	
	/**
	 * Returns the ownerId.
	 * @return String
	 */
	public String getOwnerId() {
		return ownerId;
	}

	/**
	 * Sets the ownerId.
	 * @param ownerId The ownerId to set
	 */
	public void setOwnerId(String ownerId) {
		this.ownerId = ownerId;
	}

	/**
	 * Returns the id.
	 * @return String
	 */
	public String getId() {
		return id;
	}

	/**
	 * Returns the copyId.
	 * @return String
	 */
	public String getCopyId() {
		return copyId;
	}

	/**
	 * Sets the copyId.
	 * @param copyId The copyId to set
	 */
	public void setCopyId(String copyId) {
		this.copyId = copyId;
	}
	
	/**
	 * Returns the String representation of this Module entity.
	 */
	@Override public String toString(){
		return "Module:"+getId()+DELIMITER+ownerId+DELIMITER+copyId;//+" holders:"+holders;
	}
	
	///////////////////
	///// storage /////	
	///////////////////
	

	/**
	 *
	 */
	@Override public Enumeration<String> getKeys() {
		Enumeration<DataHolder> allObjects = holders.elements();
		Vector<String> keys = new Vector<String>();
		while(allObjects.hasMoreElements()){
			IBasicStoreableObject o = (IBasicStoreableObject)allObjects.nextElement();			
			keys.add(o.getStorageId());
		}
		return keys.elements();
	}

	/**
	 * @see net.anotheria.anodoc.data
	 */
	@Override public Object getObject(String key) {
		String myKey = key.substring(key.indexOf(IHelperConstants.DELIMITER)+1);
		return holders.get(myKey);
	}

	/**
	 * @see net.anotheria.anodoc.data.IBasicStoreableObject#getStorageId()
	 */
	@Override public String getStorageId() {
		return ""+id+DELIMITER+ownerId+DELIMITER+copyId;		
	}
	
	/// restoring from container
	/**
	 * Restores this module from a container. When a Module is saved, 
	 * it's not saved as is, but only it's contents (which recursively matches for contained documents too).
	 * If a Module instance is loaded, it have to be reassembled which happens in this function.
	 */
	@SuppressWarnings("unchecked")
	public void fillFromContainer(Hashtable container){
		log.debug("Filling from container, this:"+this+" container:"+container);
		Enumeration e = container.keys();
		while(e.hasMoreElements()){
			String key = (String)e.nextElement();
			Object subContainer = container.get(key);
			//System.err.println("Key:"+key);
			DataHolder assembledData = null;
			
			if (KeyUtility.isDocument(key)){
 				assembledData = assembleDocument(key, subContainer, null);
			}
			
			try{
				if (KeyUtility.isList(key)){
					assembledData = assembleList(key, subContainer);
				}
			}catch(Exception ex){
				log.warn("Couldn't assemble "+key+" cause: "+ex.getMessage());
			}
			
			if (assembledData==null)
				log.warn("Unsupported assemblee with key: "+key);
			else
				putDataHolder(assembledData);
				
		}
		log.info("Assembled module:"+this);
	}
	
	/**
	 * Assembles a document, used by  fillFromContainer.
	 * @param key	the key of the object.
	 * @param o		the object which represented the assemblee (Hashtable for Document)
	 * @param context	the context in which the resulting Document previously existed and will exists now.
	 * @return the newly assembled Document
	 */
	private Document assembleDocument(String key, Object o, DataHolder context){
		@SuppressWarnings("unchecked")Hashtable myContainer = (Hashtable)o;
		String myName = KeyUtility.getDocumentName(key);
		
		log.info("Assembling document with name:"+myName);
		Document doc = context == null ?
			 moduleFactory.createDocument(myName) :
			 moduleFactory.createDocument(myName, context);
		
		//jetzt versuchen wir properties auszulesen!
			 @SuppressWarnings("unchecked")Enumeration e = myContainer.keys();
		while(e.hasMoreElements()){
			String aKey = (String)e.nextElement();
			Object anObj = myContainer.get(aKey);
			
			if (anObj instanceof Property){
				doc.putProperty((Property)anObj);
			}else{
				//dirst decided...whether list or not...
				@SuppressWarnings("unchecked")Hashtable tmp = (Hashtable)anObj;
				DataHolder containedHolder = null;
				if (tmp.containsKey(IHelperConstants.IDENTIFIER_KEY) &&
					((StringProperty)tmp.get(IHelperConstants.IDENTIFIER_KEY)).getString().equals(IHelperConstants.IDENTIFIER_DOCUMENT))
					containedHolder = assembleDocument(aKey, anObj, doc);
				else
					containedHolder = assembleList(aKey, anObj);
				doc.addDataHolder(containedHolder);
				//System.err.println("YET Unsupported type!"+anObj.getClass());
			}
				
		}
		return doc;
	}

	/**
	 * Assembles a list, used by  fillFromContainer.
	 * @param key	the key of the object.
	 * @param o		the object which represented the assemblee (Hashtable)
	 * @return the newly assembled DocumentList
	 */
	@SuppressWarnings("unchecked")
	private DocumentList assembleList(String key, Object o){
		Hashtable myContainer = (Hashtable)o;
		String myName = KeyUtility.getDocumentName(key);
		
		log.info("Assembling list with name:"+myName);
		DocumentList list = moduleFactory.createDocumentList(myName);
		
		//jetzt versuchen wir properties auszulesen!
		Enumeration e = myContainer.keys();
		Document arr[] = new Document[myContainer.size()];
		
		while(e.hasMoreElements()){
			String aKey = (String)e.nextElement();
			Object anObj = myContainer.get(aKey);
			
			//wir koennen nur documents speichern...
			
			int pos = Integer.parseInt(KeyUtility.getListPos(aKey));
			String docKey = KeyUtility.getKeyFromListKey(aKey);
			Document doc = assembleDocument(docKey, anObj, list);
			arr[pos] = doc;
				
				
		}
		
		for (int i=0; i<arr.length; i++)
			list.addDocument(arr[i]);

		return list;
		
	}

	/**
	 * Returns the moduleFactory.
	 * @return IModuleFactory
	 */
	public IModuleFactory getModuleFactory() {
		return moduleFactory;
	}

	/**
	 * Sets the moduleFactory.
	 * @param myFactory The moduleFactory to set
	 */
	public void setModuleFactory(IModuleFactory myFactory) {
		this.moduleFactory = myFactory;
	}
	
	/**
	 * Overwrite this to return some statistical information for netStats (activity module)
	 * which would be save automatically on each module save.<br>
	 * Return -1 if you don't want to provide any statistical information
	 */	
	public long getStatisticalInformation(){
		return -1;
	}
	
	/**
	 * Returns the size of the module in bytes. Actually it checks the size of all contained Documents and DocumentLists 
	 * and returns the cumulated value.
	 * @return the size of the data in the module in bytes.
	 */
	public long getSizeInBytes(){
		Enumeration<DataHolder> dataHolders = holders.elements();
		long sum = 0;
		while (dataHolders.hasMoreElements()){
			sum += ((DataHolder)dataHolders.nextElement()).getSizeInBytes();
		}
		return sum;
	}
	
	/**
	 * Returns the names of the holders contained in this module. 
	 */
	public Enumeration<String> getHolderNames(){
		return holders.keys();
	}

	/**
	 * Returns the id holder for a given document (name).
	 * @param docName
	 * @return
	 */
	protected IDHolder _getIdHolder(String docName){
		try{
			IDHolder h = (IDHolder)getDocument(docName);
			return h;
		}catch(NoSuchDocumentException e){}
		return new IDHolder(docName);
	}
	/**
	 * Creates an XMLNode for XML export.
	 * @return
	 */
	public XMLNode toXMLNode(){
		XMLNode root = new XMLNode("module");
		
		root.addAttribute(new XMLAttribute("moduleId", getId()));
		root.addAttribute(new XMLAttribute("copyId", getCopyId()));
		root.addAttribute(new XMLAttribute("ownerId", getOwnerId()));
		root.addAttribute(new XMLAttribute("size", ""+getSizeInBytes()));
		
		Collection<DataHolder> myDataHolders = holders.values();
		for (DataHolder dh : myDataHolders){
			root.addChildNode(dh.toXMLNode());
		}
		
		return root;
	}

}
