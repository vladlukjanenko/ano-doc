package net.anotheria.anodoc.util;

import java.util.List;

import org.apache.log4j.Logger;

import net.anotheria.anodoc.data.Module;
import net.anotheria.anodoc.query.Predicate;
import net.anotheria.anodoc.service.IModuleFactory;
import net.anotheria.anodoc.service.IModuleStorage;
import net.anotheria.anodoc.service.NoStoredModuleEntityException;
import net.anotheria.anodoc.service.StorageFailureException;

/**
 * This class behaves like {@link biz.beaglesoft.bgldoc.service.IModuleStorage} but
 * delegates all method calls to an instance of {@link biz.beaglesoft.bgldoc.util.ICommonModuleStorage}.
 */
public class CommonModuleStorageWrapper implements IModuleStorage {

	private String moduleId;
	private IModuleFactory factory;
	private ICommonModuleStorage delegate;
	
	private static Logger log;
	
	static{
		log = Logger.getLogger(CommonModuleStorageWrapper.class);
	}
	
	public CommonModuleStorageWrapper(String aModuleId, IModuleFactory aFactory, ICommonModuleStorage aDelegate){
		moduleId = aModuleId;
		factory = aFactory;
		delegate = aDelegate;
	}
	
	/**
	 * @see biz.beaglesoft.bgldoc.service.IModuleStorage#saveModule(biz.beaglesoft.bgldoc.data.BGLModule)
	 */
	public void saveModule(Module module) throws StorageFailureException{
		try {
			delegate.saveModule(module);
		} catch (CommonModuleStorageException e) {
			//und nun ?? das interface erlaubt ja nur runtimes .. und das ist bloed ...
			//das ist nicht bloed, man wirft sie einfach <- lro.
			//runtimes sind immer bloed. zumal an dieser stelle klar ist das es verschieden arten von
			//fehlern geben kann. Die sollten dann auch dem entsprechend behandelt werden koennen. <- cho
			log.warn("saveModule", e);
			throw new StorageFailureException(e.getMessage()); 
		}
	}

	
	/**
	 * @see biz.beaglesoft.bgldoc.service.IModuleStorage#loadModule(java.lang.String, java.lang.String)
	 */
	public Module loadModule(String ownerId, String copyId) throws NoStoredModuleEntityException, StorageFailureException{
		try {
			Module module = delegate.loadModule(moduleId,ownerId,copyId,factory);
			if(module == null){
				throw new NoStoredModuleEntityException("haeh ??");
			}
			return module;
		} catch (NoStoredModuleEntityException e) {
			throw e;
		} catch (CommonModuleStorageException e) {
			log.warn("loadModule", e);
			throw new StorageFailureException(e.getMessage());
		}
	}
	
	/**
	 * @see biz.beaglesoft.bgldoc.service.IModuleStorage#deleteModule(java.lang.String, java.lang.String)
	 */
	public void deleteModule(String ownerId, String copyId) throws StorageFailureException{
		try{
			delegate.deleteModule(moduleId,ownerId,copyId);
		}catch(Exception ex){
			log.error("deleteModule", ex);
			throw new StorageFailureException(ex.getMessage());
		}
	}
	
	/**
	 * @see biz.beaglesoft.bgldoc.service.IModuleStorage#executeQueryOnDocuments(BasicPredicate)
	 */
	public List executeQueryOnDocuments(Predicate p) throws StorageFailureException{
		try{
			return delegate.executeQueryOnDocuments(p, factory);
		}catch(Exception e){
			log.warn("executeQueryOnDocuments", e);
			throw new StorageFailureException(e.getMessage());
		}
		//return new Vector(0);
	}

}
