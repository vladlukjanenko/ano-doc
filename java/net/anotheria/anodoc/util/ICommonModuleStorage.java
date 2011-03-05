package net.anotheria.anodoc.util;

import net.anotheria.anodoc.data.Module;
import net.anotheria.anodoc.service.IModuleFactory;

public interface ICommonModuleStorage {

	/**
	 * Saves the given instance permanently. 
	 */
	void saveModule(Module module) throws CommonModuleStorageException;

	/**
	 * Loads the specified instance. 
	 */
	Module loadModule(String moduleId, String ownerId, String copyId, IModuleFactory factory) throws CommonModuleStorageException;

	/**
	 * Delete the specified instance. 
	 */
	void deleteModule(String moduleId, String ownerId, String copyId) throws CommonModuleStorageException;

}
