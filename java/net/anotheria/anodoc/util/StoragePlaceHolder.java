package net.anotheria.anodoc.util;

import java.util.List;

import net.anotheria.anodoc.data.Document;
import net.anotheria.anodoc.data.Module;
import net.anotheria.anodoc.service.IModuleStorage;
import net.anotheria.anodoc.service.NoStoredModuleEntityException;

/**
 * This class represents a IModuleStorage which DOES NOT store 
 * anything, and act as a placeholder instead. This is useful for view testing sometimes.
 */
public class StoragePlaceHolder implements IModuleStorage{

	/**
	 * Always throws new NoStoredModuleEntityException("placeholder storage") 
	 */
	@Override public Module loadModule(String ownerId, String copyId)
		throws NoStoredModuleEntityException {
		throw new NoStoredModuleEntityException("placeholder storage");
	}
 
	/**
	 * Does nothing
	 */
	@Override public void saveModule(Module module) {
	}

	/**
	 * Does nothing
	 */	
	@Override public void deleteModule(String ownerId, String copyId){
	}
}
