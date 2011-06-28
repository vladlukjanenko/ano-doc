package net.anotheria.anodoc.service;

import net.anotheria.anodoc.data.Module;
import net.anotheria.asg.util.listener.IModuleListener;

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
	 * @return loaded module
	 */
	Module loadModule(String ownerId, String copyId) throws NoStoredModuleEntityException, StorageFailureException;
	
	/**
	 * Delete the specified instance. 
	 */
	void deleteModule(String ownerId, String copyId) throws StorageFailureException;

	/**
	 * Adds a module listener.
	 * @param listener the listener to add.
	 */
	void addModuleListener(IModuleListener listener);

	/**
	 * Removes the module listener from the module storage.
	 * @param listener the listener to remove.
	 */
	void removeModuleListener(IModuleListener listener);

}
