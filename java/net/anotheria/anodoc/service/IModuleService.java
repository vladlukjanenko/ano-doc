package net.anotheria.anodoc.service;

import net.anotheria.anodoc.data.Module;
import net.anotheria.asg.util.listener.IModuleListener;

/**
 * This interface describes the locally available service for Module management.<br>
 * It provides functions for configuration of itself and for data access/storage.
 */
public interface IModuleService {
	
	/**
	 * This function retrieves the appropriate Module from the storage and returns it.
	 * If the create flag is set, and no module instance is available (NoStoredModuleEntityException) 
	 * a new Module instance will be created by the previously attached Factory
	 * and returned.	 
	 * @param ownerId the id of the owner of this module.
	 * @param moduleId the id of the module
	 * @param copyId the copy id.
	 * @param create if true and the module doesn't exists yet, it will be created.
	 * @return retrieved Module
	 * @throws NoStorageForModuleException
	 * @throws NoFactoryForModuleException
	 * @throws NoStoredModuleEntityException
	 * @throws StorageFailureException
	 */
	Module getModule(String ownerId, String moduleId, String copyId, boolean create) 
		throws NoStorageForModuleException, NoFactoryForModuleException, NoStoredModuleEntityException, StorageFailureException;

	Module getModule(String ownerId, String moduleId, boolean create) 
		throws NoStorageForModuleException, NoFactoryForModuleException, NoStoredModuleEntityException, StorageFailureException;

	Module getModule(String ownerId, String moduleId) 
		throws NoStorageForModuleException, NoFactoryForModuleException, NoStoredModuleEntityException, StorageFailureException;

	/**
	 * Same as return getModule(ownerId, moduleId, copyId, false); in current implementation.
	 */
	Module getModule(String ownerId, String moduleId, String copyId) 
		throws NoStorageForModuleException, NoFactoryForModuleException, NoStoredModuleEntityException, StorageFailureException;


	/**
	 * Stores the given module in the appropriate storage.
	 * @param module the module to store.
	 * @throws NoStorageForModuleException thrown if no storage for this moduleId is attached.
	 * @throws StorageFailureException thrown if the storage failed.
	 */
	void storeModule(Module module) throws NoStorageForModuleException, StorageFailureException;

	/**
	 * Attaches a factory for a given moduleId.
	 * @param moduleId
	 * @param storage
	 */
	void attachModuleFactory(String moduleId, IModuleFactory factory);	

	/**
	 * Attaches a storage for a given moduleId.
	 * @param moduleId
	 * @param storage
	 */
	void attachModuleStorage(String moduleId, IModuleStorage storage);
	
	/**
	 * Deletes the given module from the appropriate storage.   
	 */
	void deleteModule(Module module) throws NoStorageForModuleException, StorageFailureException;
	
	/**
	 * Deletes the module identified by the tuple (ownerId | moduleId | copyId) from the appropriate storage.   
	 */
	void deleteModule(String ownerId, String moduleId, String copyId) throws NoStorageForModuleException, StorageFailureException;
	
	/**
	 * Deletes the module identified by the tuple (ownerId | moduleId) from the appropriate storage. The copy id
	 * is assumed to be the standart copyId.   
	 */
	void deleteModule(String ownerId, String moduleId) throws NoStorageForModuleException, StorageFailureException;

	/**
	 * Adds listener for module.
	 * @param moduleId module id of module.
	 * @param ownerId owner id of module.
	 * @param aModuleListeners listener to add.
	 */
	void addModuleListener(String moduleId, String ownerId, IModuleListener aModuleListeners);

	/**
	 * Removes listener for module.
	 * @param moduleId id of module.
	 * @param ownerId owner id for module.
	 */
	void removeModuleListener(String moduleId, String ownerId);

}
 