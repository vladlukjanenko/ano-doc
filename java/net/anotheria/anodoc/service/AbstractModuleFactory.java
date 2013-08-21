package net.anotheria.anodoc.service;

import net.anotheria.anodoc.data.DataHolder;
import net.anotheria.anodoc.data.Document;
import net.anotheria.anodoc.data.DocumentList;
import net.anotheria.anodoc.data.IDHolder;
import net.anotheria.anodoc.data.Module;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;

/** 
 * Base imlementation of IModuleFactory interface which only 
 * demands createModule method.
 */
public abstract class AbstractModuleFactory implements IModuleFactory, Serializable{
	
	private static final long serialVersionUID=1580293076199614251L;
	
	/**
	 * {@link Logger} instance.
	 */
	private Logger log;
	
	protected AbstractModuleFactory(){
		log = LoggerFactory.getLogger(this.getClass());
	}
	
	protected Logger getLog(){
		return log;
	}
	/**
	 * Creates a new Document from a name and a context (like list). If this function is not overwritten, it call createDocument(name).
	 * @return new Document
	 */
	public Document createDocument(String name, DataHolder context) {
		getLog().debug("This Factory doesn't overwrite create document with context (Doc:"+name+", context:"+context+")"); 
		getLog().debug("will call createDocument(name) instead.");
		return createDocument(name);
	}
	
	/**
	 * Creates a new Document from a name and a typeidentifier. If this function is not overwritten, it call createDocument(name).
	 * @retun new Document
	 */
	public Document createDocument(String name, String typeIdentifier){
		getLog().debug("This Factory doesn't overwrite create document with identifier (Doc:"+name+", identifier:"+typeIdentifier+")"); 
		getLog().debug("will call createDocument(name) instead.");
		return createDocument(name);
	}


	/**
	 * Creates and returns a new Document. This method should be overwritten by the extending class,
	 * since usage of Document directly in your modell is not fitting in the concept of ano-doc.
	 * @return newly created Document.
	 */
	@Override public Document createDocument(String id) {
		if (id.startsWith(IDHolder.DOC_ID_HOLDER_PRE))
			return new IDHolder(id);
		
		getLog().debug("This Factory doesn't overwrite create document (docname:"+id+")");
		return new Document(id);
	}

	/**
	 * Creates and returns a DocumentList.
	 * @return newly created DocumentList
	 */
	public <D extends Document> DocumentList<D> createDocumentList(String name, DataHolder context) {
		getLog().debug("This Factory doesn't overwrite create document list(listname:"+name+")");
		return new DocumentList<D>(name);
	}

	/**
	 * Creates and returns a DocumentList.
	 * @return newly created DocumentList
	 */
	public <D extends Document> DocumentList<D> createDocumentList(String name) {
		getLog().debug("This Factory doesn't overwrite create document list(listname:"+name+")");
		return new DocumentList<D>(name);
	}


	@Override public final Module createModule(String ownerId, String copyId) {
		Module module = recreateModule(ownerId, copyId);
		module.setOwnerId(ownerId);
		module.setCopyId(copyId);
		return module;
	}
	
	/**
	 * Recreates a Module. This method is called by the AbstractModuleFactory in
	 * the createModule method. It sets the proper owner and copy ids in the newly 
	 * created Module instance, so the extending class doesn't need to do it itself.
	 * @return recreated module
	 */
	public abstract Module recreateModule(String ownerId, String copyId);
	

}
