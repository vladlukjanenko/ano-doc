package net.anotheria.anodoc.util;

import net.anotheria.anodoc.data.Module;
import net.anotheria.anodoc.service.IModuleStorage;
import net.anotheria.anodoc.service.NoStoredModuleEntityException;
import net.anotheria.asg.util.listener.IModuleListener;

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

	/**
	 *
	 * Does nothing.
	 */
	@Override public void addModuleListener(IModuleListener listener) {
	}

	/**
	 *
	 * Does nothing.
	 */
	@Override public void removeModuleListener(IModuleListener listener) {
	}

}
