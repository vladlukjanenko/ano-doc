package net.anotheria.anodoc.service;

import net.anotheria.anodoc.data.Module;

/**
 * Storage for Module instances.
 */
public interface IModuleStorage {

	/**
	 * Saves the given instance permanently. 
	 */
	void saveModule(Module module) throws StorageFailureException;
	
	/**
	 * Loads the specified instance. 
	 */
	Module loadModule(String ownerId, String copyId) throws NoStoredModuleEntityException, StorageFailureException;
	
	/**
	 * Delete the specified instance. 
	 */
	void deleteModule(String ownerId, String copyId) throws StorageFailureException;
	
}
