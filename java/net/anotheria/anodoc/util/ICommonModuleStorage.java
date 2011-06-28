package net.anotheria.anodoc.util;

import net.anotheria.anodoc.data.Module;
import net.anotheria.anodoc.service.IModuleFactory;

/**
 * Interface for module storage.
 */
public interface ICommonModuleStorage {

	/**
	 * Saves the given instance permanently.
	 * @param module module to save
	 */
	void saveModule(Module module) throws CommonModuleStorageException;

	/**
	 * Loads the specified instance.
	 * @return loaded module
	 */
	Module loadModule(String moduleId, String ownerId, String copyId, IModuleFactory factory) throws CommonModuleStorageException;

	/**
	 * Delete the specified instance.
	 *
	 * @param moduleId id of module
	 * @param ownerId id of owner
	 * @param copyId id of copy
	 * @throws CommonModuleStorageException
	 */
	void deleteModule(String moduleId, String ownerId, String copyId) throws CommonModuleStorageException;

}
