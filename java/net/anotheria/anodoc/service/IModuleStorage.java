package net.anotheria.anodoc.service;

import java.util.List;

import net.anotheria.anodoc.data.Document;
import net.anotheria.anodoc.data.Module;
import net.anotheria.anodoc.query.Predicate;

/**
 * Storage for Module instances.
 */
public interface IModuleStorage {

	/**
	 * Saves the given instance permanently. 
	 */
	public void saveModule(Module module) throws StorageFailureException;
	
	/**
	 * Loads the specified instance. 
	 */
	public Module loadModule(String ownerId, String copyId) throws NoStoredModuleEntityException, StorageFailureException;
	
	/**
	 * Delete the specified instance. 
	 */
	public void deleteModule(String ownerId, String copyId) throws StorageFailureException;
	
	/**
	 * Executes the specified query on all stored module instances. This method isn't supported by all implementations. 
	 */
	public List<Document> executeQueryOnDocuments(Predicate p)throws StorageFailureException;
}
