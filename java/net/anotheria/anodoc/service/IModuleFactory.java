package net.anotheria.anodoc.service;

import net.anotheria.anodoc.data.DataHolder;
import net.anotheria.anodoc.data.Document;
import net.anotheria.anodoc.data.DocumentList;
import net.anotheria.anodoc.data.Module;

/**
 * This factory is responsible for creating new modules, if a service should be 
 * able to create new modules.
 * It also should have the ability to create new Documents in the
 * Module Modell.
 * @since 1.0
 * @see biz.beaglesoft.bgldoc.service.AbstractModuleFactory
 */
public interface IModuleFactory {
	/**
	 * Creates a new BGLModule Instance for given owner and copy ids.
	 */
	public Module createModule(String ownerId, String copyId);
	
	/**
	 * Creates a new BGLDocument with given name.
	 */
	public Document createDocument(String name);
	
	/**
	 * Creates a new BGLDocument with given name and a type identifier. 
	 */
	public Document createDocument(String name, String typeIdentifier);
	
	/**
	 * Creates a new BGLDocument with given name in a given context.
	 * A context can be another document which holds the newly created document,
	 * or a list.
	 */
	public Document createDocument(String name, DataHolder context);
	
	/**
	 * Creates a new BGLDocumentList with given name and without a context. Normally
	 * you don't need this method, since list is seldom overwritten. 
	 */
	public DocumentList createDocumentList(String name);
	/**
	 * Creates a new BGLDocumentList with given name and context.
	 */
	public DocumentList createDocumentList(String name, DataHolder context);
	
}
