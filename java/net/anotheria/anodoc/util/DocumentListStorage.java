package net.anotheria.anodoc.util;

import java.net.InetAddress;
import java.util.List;

import org.apache.log4j.Logger;
import org.apache.log4j.NDC;

import net.anotheria.anodoc.data.DataHolder;
import net.anotheria.anodoc.data.Document;
import net.anotheria.anodoc.data.DocumentList;
import net.anotheria.anodoc.service.IModuleFactory;

/**
 * Database class for {@link biz.beaglesoft.bgldoc.data.BGLDocumentList}.<br>
 * Only {@link biz.beaglesoft.bgldoc.util.CommonJDOModuleStorage} uses this class
 * to be insusceptable against class changes.
 */
class DocumentListStorage {

	private static Logger log;
	private static String _myAddress_;
	static {
		log = Logger.getLogger(DocumentListStorage.class);
		try {
			_myAddress_ = InetAddress.getLocalHost().toString();
		} catch (Exception e) {
			_myAddress_ = "unknown: " + e.getMessage();
		}
	}

	/**
	 * the list of documents this list contains
	 */
	private DocumentStorage[] docs;
	
	/**
	 * the name/id of this list
	 */
	private String name;
	
	private DocumentListStorage(){}
	
	DocumentListStorage(DocumentList bglDocumentList, String moduleId) {
		initializeFromList(bglDocumentList,moduleId);
	}
	
	/**
	 * This method initializes the instance of this class out of a {@link biz.beaglesoft.bgldoc.data.BGLDocumentList}
	 */
	private void initializeFromList(DocumentList bglDocumentList, String moduleId) {
		if (log.isDebugEnabled()) {
			NDC.push(_myAddress_);
			log.debug("handling "+bglDocumentList.getList().size()+" elements");
			NDC.pop();
		}
		name = bglDocumentList.getId();
		List originals = bglDocumentList.getList();
		docs = new DocumentStorage[originals.size()];
		for(int i = 0; i < originals.size(); i++){
			Document bglDoc = (Document)originals.get(i);
			docs[i] = new DocumentStorage(bglDoc, moduleId);
		}
	}
	
	/**
	 * returns the {@link biz.beaglesoft.bgldoc.data.BGLDocumentList} the current instance represents.<br> 
	 * The instance is created byb using the passed factory.<br>
	 * Therefore it is possible to recreate even subclasses out of this common storage class by just 
	 * passing the right factory.
	 */
	public DocumentList getDocumentList(IModuleFactory factory, DataHolder context) {
		DocumentList list = factory.createDocumentList(getName(),context);
		for(int i = 0; i < docs.length; i++){
			list.addDocument(docs[i].getDocument(factory,list));
		}
		return list;
	}

	/**
	 * Returns the name.
	 */
	public String getName() {
		return name;
	}

}
