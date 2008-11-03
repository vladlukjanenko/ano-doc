package net.anotheria.anodoc.util;

import java.util.List;

import net.anotheria.anodoc.data.Document;
import net.anotheria.anodoc.data.Module;
import net.anotheria.anodoc.query.Predicate;
import net.anotheria.anodoc.service.IModuleStorage;
import net.anotheria.anodoc.service.NoStoredModuleEntityException;

/**
 * This class represents a IModuleStorage which DOES NOT store 
 * anything, and act as a placeholder instead. This is useful for view testing sometimes.
 */
public class StoragePlaceHolder implements IModuleStorage{

	/**
	 * Always throws new NoStoredModuleEntityException("placeholder storage") 
	 * @see biz.beaglesoft.bgldoc.service.IModuleStorage#loadModule(String, String)
	 */
	public Module loadModule(String ownerId, String copyId)
		throws NoStoredModuleEntityException {
		throw new NoStoredModuleEntityException("placeholder storage");
	}
 
	/**
	 * Does nothing
	 * @see biz.beaglesoft.bgldoc.service.IModuleStorage#saveModule(BGLModule)
	 */
	public void saveModule(Module module) {
	}

	/**
	 * Does nothing
	 */	
	public void deleteModule(String ownerId, String copyId){
	}

	/**
	 * Throws a runtime exception - not implemented.
	 * @see biz.beaglesoft.bgldoc.service.IModuleStorage#executeQueryOnDocuments(BasicPredicate)
	 */
	public List<Document> executeQueryOnDocuments(Predicate p) {
		throw new RuntimeException("Not implemented");
	}

}
