package net.anotheria.anodoc.util;

import java.util.List;

import net.anotheria.anodoc.data.Document;
import net.anotheria.anodoc.data.Module;
import net.anotheria.anodoc.query.Predicate;
import net.anotheria.anodoc.service.IModuleFactory;

/**
 * This interface can be thought of as an extension to {@link biz.beaglesoft.bgldoc.service.IModuleStorage}. 
 * The difference is that you need always you own instance of {@link biz.beaglesoft.bgldoc.service.IModuleStorage}
 * for each module; in this case one instance is enough for all. 
 * This interface defines that difference by just adding
 * the moduleId to each method of {@link biz.beaglesoft.bgldoc.service.IModuleStorage} and taking care of it.
 */
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

	/**
	 * Executes the specified query on all stored module instances. This method isn't supported by all implementations. 
	 */
	List<Document> executeQueryOnDocuments(Predicate p, IModuleFactory fac) throws CommonModuleStorageException;
}
