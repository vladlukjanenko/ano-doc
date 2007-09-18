package net.anotheria.anodoc.util;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import org.apache.log4j.Logger;
import org.apache.log4j.NDC;

import net.anotheria.anodoc.data.DataHolder;
import net.anotheria.anodoc.data.Document;
import net.anotheria.anodoc.data.DocumentList;
import net.anotheria.anodoc.data.ListProperty;
import net.anotheria.anodoc.data.Property;
import net.anotheria.anodoc.data.TextProperty;
import net.anotheria.anodoc.service.IModuleFactory;

/**
 */
class DocumentStorage {

	private static Logger log;
	private static String _myAddress_;
	static {
		log = Logger.getLogger(DocumentStorage.class);
		try {
			_myAddress_ = InetAddress.getLocalHost().toString();
		} catch (Exception e) {
			_myAddress_ = "unknown: " + e.getMessage();
		}
	}

	/**
	 * the name/id of this document
	 */
	private String name;
	
	/**
	 * the type identifier.<br>
	 * usefull for queries. Using the type identifier makes it possible to 
	 * query just special documents that belong to a special module.
	 */
	private String typeId;
	
	/**
	 * the moduleId of the module this document belongs to
	 */
	private String moduleId;
	
	private PropertyListStorage[] listProps;
	
	private PropertyStorage[] props;
	
	private DocumentStorage[] docs;
	
	private DocumentListStorage[] lists;
	
	private DocumentStorage(){}
	
	DocumentStorage(Document document, String moduleId) {
		initializeFromDocument(document, moduleId);
	}

	/**
	 * This method initializes the instance of this class out of a {@link biz.beaglesoft.bgldoc.data.BGLDocument}
	 */
	private void initializeFromDocument(Document document, String moduleId) {
		name = document.getId();
		typeId = document.getTypeIdentifier();
		this.moduleId = moduleId; 
		Enumeration keys = document.getKeys();
		List _docs = new ArrayList();
		List _lists = new ArrayList();
		List _props = new ArrayList();
		List _listProps = new ArrayList();
		while(keys.hasMoreElements()){
			Object o = document.getObject((String)keys.nextElement());
			if (log.isDebugEnabled()) {
				NDC.push(_myAddress_);
				log.debug("handling object of "+o.getClass());
				NDC.pop();
			}
			if(o instanceof ListProperty){
				_listProps.add(new PropertyListStorage((ListProperty)o));
			}else if(o instanceof TextProperty){
				_props.add(new TextPropertyStorage((TextProperty)o));
			}else if(o instanceof Property){
				_props.add(new PropertyStorage((Property)o));
			}else if(o instanceof Document){
				_docs.add(new DocumentStorage((Document)o,moduleId));
			}else if(o instanceof DocumentList){
				_lists.add(new DocumentListStorage((DocumentList)o, moduleId));
			}
		}
		lists = new DocumentListStorage[_lists.size()];
		lists = (DocumentListStorage[])_lists.toArray(lists);
		docs = new DocumentStorage[_docs.size()];
		docs = (DocumentStorage[])_docs.toArray(docs);
		props = new PropertyStorage[_props.size()];
		props = (PropertyStorage[])_props.toArray(props);
		listProps = new PropertyListStorage[_listProps.size()];
		listProps = (PropertyListStorage[])_listProps.toArray(listProps);
	}

	/**
	 * returns the {@link biz.beaglesoft.bgldoc.data.BGLDocument} the current instance represents.<br> 
	 * The instance is created by using the passed factory.<br>
	 * Therefore it is possible to recreate even subclasses out of this common storage class by just 
	 * passing the right factory.
	 */
	Document getDocument(IModuleFactory factory, DataHolder context) {
		Document doc = factory.createDocument(getName(),getTypeId());
		for(int i = 0; i < docs.length; i++){
			doc.putDocument(docs[i].getDocument(factory,doc));
		}
		for(int i = 0; i < lists.length; i++){
			doc.putList(lists[i].getDocumentList(factory,doc));
		}
		for(int i = 0; i < props.length; i++){
			doc.putProperty(props[i].getProperty());
		}
		for(int i = 0; i < listProps.length; i++){
			doc.putProperty(listProps[i].getProperty());
		}
		return doc;
	}

	/**
	 * Returns the name.
	 * @return String
	 */
	public String getName() {
		return name;
	}

	/**
	 * Returns the typeId.
	 * @return String
	 */
	public String getTypeId() {
		return typeId;
	}

}
