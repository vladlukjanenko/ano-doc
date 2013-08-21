package net.anotheria.anodoc.util;

import net.anotheria.anodoc.data.Module;
import net.anotheria.anodoc.service.IModuleFactory;
import net.anotheria.anodoc.service.IModuleStorage;
import net.anotheria.anodoc.service.NoStoredModuleEntityException;
import net.anotheria.anodoc.service.StorageFailureException;
import net.anotheria.asg.util.listener.IModuleListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class behaves like {@link net.anotheria.anodoc.service.IModuleStorage} but
 * delegates all method calls to an instance of {@link net.anotheria.anodoc.util.ICommonModuleStorage}.
 */
public class CommonModuleStorageWrapper implements IModuleStorage {

	/**
	 * {@link Logger} instance.
	 */
	private static final Logger LOGGER = LoggerFactory.getLogger(CommonModuleStorageWrapper.class);

	private String moduleId;
	private IModuleFactory factory;
	private ICommonModuleStorage delegate;

	public CommonModuleStorageWrapper(String aModuleId, IModuleFactory aFactory, ICommonModuleStorage aDelegate){
		moduleId = aModuleId;
		factory = aFactory;
		delegate = aDelegate;
	}
	
	@Override public void saveModule(Module module) throws StorageFailureException{
		try {
			delegate.saveModule(module);
		} catch (CommonModuleStorageException e) {
			//und nun ?? das interface erlaubt ja nur runtimes .. und das ist bloed ...
			//das ist nicht bloed, man wirft sie einfach <- lro.
			//runtimes sind immer bloed. zumal an dieser stelle klar ist das es verschieden arten von
			//fehlern geben kann. Die sollten dann auch dem entsprechend behandelt werden koennen. <- cho
			LOGGER.warn("saveModule", e);
			throw new StorageFailureException(e.getMessage()); 
		}
	}

	@Override public Module loadModule(String ownerId, String copyId) throws NoStoredModuleEntityException, StorageFailureException{
		try {
			Module module = delegate.loadModule(moduleId,ownerId,copyId,factory);
			if(module == null){
				throw new NoStoredModuleEntityException("haeh ??");
			}
			return module;
		} catch (NoStoredModuleEntityException e) {
			throw e;
		} catch (CommonModuleStorageException e) {
			LOGGER.warn("loadModule", e);
			throw new StorageFailureException(e.getMessage());
		}
	}
	
	@Override public void deleteModule(String ownerId, String copyId) throws StorageFailureException{
		try{
			delegate.deleteModule(moduleId,ownerId,copyId);
		}catch(Exception ex){
			LOGGER.error("deleteModule", ex);
			throw new StorageFailureException(ex.getMessage());
		}
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
