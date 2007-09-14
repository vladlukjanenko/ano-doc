package net.anotheria.anodoc.util;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import org.apache.log4j.Logger;
import org.apache.log4j.NDC;

import net.anotheria.anodoc.data.Document;
import net.anotheria.anodoc.data.DocumentList;
import net.anotheria.anodoc.data.Module;
import net.anotheria.anodoc.service.IModuleFactory;

/**
 * Database class for {@link biz.beaglesoft.bgldoc.data.BGLModule}.<br>
 * Only {@link biz.beaglesoft.bgldoc.util.CommonJDOModuleStorage} uses this class
 * to be insusceptable against class changes.
 */
class ModuleStorage {
	
	private static Logger log;
	private static String _myAddress_;
	static {
		log = Logger.getLogger(ModuleStorage.class);
		try {
			_myAddress_ = InetAddress.getLocalHost().toString();
		} catch (Exception e) {
			_myAddress_ = "unknown: " + e.getMessage();
		}
	}
	
	/**
	 * the list of {@link biz.beaglesoft.bgldoc.data.BGLDocument}s this document contains
	 */
	private DocumentStorage[] docs;
	
	/**
	 * the list of {@link biz.beaglesoft.bgldoc.data.BGLDocumentList}s this document contains
	 */
	private DocumentListStorage[] lists;
	
	private String copyId;
	private String ownerId;
	private String moduleId;

	private ModuleStorage(){}

	ModuleStorage(Module module){
		initializeFromModule(module);
	}

	/**
	 * This method initializes the instance of this class out of a {@link biz.beaglesoft.bgldoc.data.BGLModule}
	 */
	private void initializeFromModule(Module module) {
		copyId = module.getCopyId();
		moduleId = module.getId();
		ownerId = module.getOwnerId();
		Enumeration keys = module.getKeys();
		List _docs = new ArrayList<DocumentStorage>();
		List _lists = new ArrayList<DocumentStorage>();
		while(keys.hasMoreElements()){
			Object o = keys.nextElement();
			handleObject(module.getObject((String)o),_docs,_lists);
		}
		docs = new DocumentStorage[_docs.size()];
		docs = (DocumentStorage[])_docs.toArray(docs);
		lists = new DocumentListStorage[_lists.size()];
		lists = (DocumentListStorage[])_lists.toArray(lists);
	}
	
	/**
	 * helper method which decides what kind of object to handle.<br>
	 * Two are possible:
	 * <ul>
	 * <li>{@link biz.beaglesoft.bgldoc.data.BGLDocument}</li>
	 * <li>{@link biz.beaglesoft.bgldoc.data.BGLDocumentList}</li>
	 * </ul>
	 * These two types are then converted into the respective database classes.
	 */
	private void handleObject(Object o, List _docs, List _lists) {
		if (log.isDebugEnabled()) {
			NDC.push(_myAddress_);
			log.debug("handleObject of "+o.getClass());
			NDC.pop();
		}
		if(o instanceof Document){
			DocumentStorage doc = new DocumentStorage((Document)o, moduleId);
			_docs.add(doc);
		}else{
			DocumentListStorage list = new DocumentListStorage((DocumentList)o, moduleId);
			_lists.add(list);
		}
	}

	/**
	 * returns the {@link biz.beaglesoft.bgldoc.data.BGLModule} the current instance represents.<br> 
	 * The instance is created by using the passed factory.<br>
	 * Therefore it is possible to recreate even subclasses out of this common storage class by just 
	 * passing the right factory.
	 */
	Module getBGLModule(IModuleFactory factory){
		Module module = factory.createModule(getOwnerId(),getCopyId());
		for(int i = 0; i < docs.length; i++){
			module.putDocument(docs[i].getDocument(factory,null));
		}
		for(int i = 0; i < lists.length; i++){
			module.putList(lists[i].getDocumentList(factory,null));
		}
		return module;
	}
	/**
	 * Returns the copyId.
	 * @return String
	 */
	public String getCopyId() {
		return copyId;
	}

	/**
	 * Returns the moduleId.
	 * @return String
	 */
	public String getModuleId() {
		return moduleId;
	}

	/**
	 * Returns the ownerId.
	 * @return String
	 */
	public String getOwnerId() {
		return ownerId;
	}

	/**
	 * Sets the copyId.
	 * @param copyId The copyId to set
	 */
	public void setCopyId(String copyId) {
		this.copyId = copyId;
	}

	/**
	 * Sets the moduleId.
	 * @param moduleId The moduleId to set
	 */
	public void setModuleId(String moduleId) {
		this.moduleId = moduleId;
	}

	/**
	 * Sets the ownerId.
	 * @param ownerId The ownerId to set
	 */
	public void setOwnerId(String ownerId) {
		this.ownerId = ownerId;
	}

}
