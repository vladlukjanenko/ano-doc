package net.anotheria.anodoc.service;

import net.anotheria.anodoc.data.Module;
import net.anotheria.asg.util.listener.IModuleListener;
import org.configureme.ConfigurationManager;
import org.configureme.annotations.AfterConfiguration;
import org.configureme.annotations.ConfigureMe;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


/**
 * An implementation of IModuleService for local usage, 
 * which supports local cache and synchronization over network.
 */
@ConfigureMe (name="anodoc.storage")
public class ModuleServiceImpl implements IModuleService, IModuleListener{

	/**
	 * {@link Logger} instance.
	 */
	private static final Logger LOGGER = LoggerFactory.getLogger(ModuleServiceImpl.class);

	/**
	 * A delimiter which is used between different parts of the unique module key.
	 */
	private static final String DELIMITER = "#";

	/**
	 * Constant used for 'copy' if none is specified.
	 */
	public static final String DEFAULT_COPY_ID = "singlecopy";

	/**
	 * The factories.
	 */
	private Map<String, IModuleFactory> factories;

	/**
	 * Map with listeners for modules.
	 */
	private Map<String, IModuleListener> moduleListeners;

	/**
	 * The module storages.
	 */
	private Map<String, IModuleStorage> storages;

	/**
	 * The local cache - loaded modules.
	 */
	private Map<String,Module> cache;

	/**
	 * The current instance. ModuleServiceImpl is a Singleton.
	 */
	private static ModuleServiceImpl instance = new ModuleServiceImpl() ;
	
	/**
	 * Returns the current (singleton) instance of this implementation. 
	 */
	protected static ModuleServiceImpl getInstance(){
		return instance;
	}

	/**
	 * Creates a new ModuleServiceImpl.
	 */
	private ModuleServiceImpl(){
		//initialize local data.
		factories = new ConcurrentHashMap<String, IModuleFactory>();
		storages  = new ConcurrentHashMap<String, IModuleStorage>();
		cache = new ConcurrentHashMap<String, Module>();
		moduleListeners = new ConcurrentHashMap<String, IModuleListener>();

		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("Created new ModuleServiceImplementation");
		}

		ConfigurationManager.INSTANCE.configure(this);
	}

	/**
	 * Attaches a factory for given module id.
	 */
	public void attachModuleFactory(String moduleId, IModuleFactory factory) {
		factories.put(moduleId, factory);
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("Attached module factory "+factory+" for moduleId:"+moduleId);
		}
	}
	
	/**
	 * Attaches a storage for given module id.
	 */
	public void attachModuleStorage(String moduleId, IModuleStorage storage){

		storages.put(moduleId, storage);
		storage.addModuleListener(this);

		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("Attached module storage "+storage+" for moduleId:"+moduleId);
		}
	}

	/**
	 * Puts the module into local cache. 
	 */	
	private void putInCache(Module module){
		String key = getKey(module);
		cache.put(key, module);
	}

	/**
	 * Removes the module from cache.
	 */	
	private void removeFromCache(String moduleId, String ownerId, String copyId){
		String key = getKey(moduleId,copyId, ownerId);
		cache.remove(key);
	}

	/**
	 * Returns module from.
	 * @param module module to serch in cache.
	 * @return module if it was in cache, otherwise null.
	 */
	private Module getModuleFromCache(Module module) {
		String key = getKey(module);
		Module cachedModule = cache.get(key);

		return cachedModule;
	}

	/**
	 * Put in cache and store.
	 */
	private void putInCacheDirty(Module module) throws NoStorageForModuleException, StorageFailureException{
		putInCache(module);
		IModuleStorage storage = storages.get(module.getId());
		if (storage==null){
			LOGGER.warn("No storage for " + module.getId() + ", " + module + " is not persistent!");
			throw new NoStorageForModuleException(module.getId());
		}
		storage.saveModule(module);
	}
	
	/**
	 * Removes the given module tuple from cache AND storage. 
	 */
	private void removeFromCacheDirty(String moduleId, String ownerId, String copyId) throws NoStorageForModuleException, StorageFailureException{
		removeFromCache(moduleId, ownerId, copyId)	;
		IModuleStorage storage = storages.get(moduleId);
		if (storage==null){
			throw new NoStorageForModuleException(moduleId);
		}
		storage.deleteModule(ownerId, copyId);
	}

	/**
	 * Same as getModule(ownerId, moduleId, copyId, false)
	 */
	public Module getModule(String ownerId, String moduleId, String copyId) 
			throws NoStorageForModuleException, NoFactoryForModuleException, NoStoredModuleEntityException, StorageFailureException{
		return getModule(ownerId, moduleId, copyId, false);
	}

	/**
	 * Same as getModule(ownerId, moduleId, default_copy_id, false)
	 */
	public Module getModule(String ownerId, String moduleId) 
			throws NoStorageForModuleException, NoFactoryForModuleException, NoStoredModuleEntityException, StorageFailureException{
		return getModule(ownerId, moduleId, DEFAULT_COPY_ID, false);
	}

	/**
 	 * Same as getModule(ownerId, moduleId, default_copy_id, create?)
	 */
	public Module getModule(
		String ownerId,
		String moduleId,
		boolean create) 
			throws NoStorageForModuleException, NoFactoryForModuleException, NoStoredModuleEntityException, StorageFailureException{
		return getModule(ownerId, moduleId, DEFAULT_COPY_ID, create);
	}
	

	@Override public Module getModule(
		String ownerId,
		String moduleId,
		String copyId,
		boolean create) 
			throws NoStorageForModuleException, NoFactoryForModuleException, NoStoredModuleEntityException, StorageFailureException{
		
		
		String key = getKey(moduleId, copyId, ownerId);
		//System.out.println("getModule() "+key+" called");

		
		//first we check if we have this module in cache.
		Module module = cache.get(key);
		
		if (module!=null){
			LOGGER.debug("Module " + key + " was in cache");
			return module;
		}
		
		
		try{
			LOGGER.debug("Trying to load module from storage:" + key);
			module = loadModule(moduleId, ownerId, copyId);
			//System.out.println("Loading from disk.");
			LOGGER.debug("Loaded module from storage.");
			putInCache(module);
			//long en = System.currentTimeMillis();
			return module;
		}catch(NoStoredModuleEntityException e){
			//log.debug("Loading failed:",e);
			if (create){
				LOGGER.debug("Creating new instance of " + moduleId + ", " + ownerId + ", " + copyId);
				//eigentlich sollte das die factory schon tun,
				//aber sicher ist sicher, oder?	 :-)
				//interessant, wer wird diesen kommentar lesen? schreibt mal
				//was zurueck, ich fand diskussionen ueber kommentare immer lustig.
				module = createModule(moduleId, ownerId, copyId); 
				module.setOwnerId(ownerId);
				module.setCopyId(copyId);
				putInCacheDirty(module);
				return module;
			}
			
			LOGGER.debug("Loading failed:", e);
			throw e;
			
		}
	}

	/**
	 * Loads the module identified by the tuple from the storage. If there is no 
	 * storage for this moduleId a NoStorageForModuleException will be thrown.
	 */	
	private Module loadModule(String moduleId, String ownerId, String copyId) throws NoStorageForModuleException, NoStoredModuleEntityException, StorageFailureException{
		IModuleStorage storage = storages.get(moduleId);
		if (storage==null){
			throw new NoStorageForModuleException(moduleId);
		}
		return storage.loadModule(ownerId, copyId);
	}

	/**
	 * Creates a new instance of the specified module using the attached factory. 
	 */	
	private Module createModule(String moduleId, String ownerId, String copyId) throws NoFactoryForModuleException{
		IModuleFactory factory = factories.get(moduleId);
		if (factory==null){
			throw new NoFactoryForModuleException(moduleId);
		}
		return factory.createModule(ownerId, copyId);
	}

	@Override public void storeModule(Module module) throws NoStorageForModuleException, StorageFailureException{
		try{
			LockHolder.prepareForSave();
			putInCacheDirty(module);
		}finally{
			LockHolder.notifySaved();
		}
	}
	
	/**
	 * Returns the cache key for given tuple. 
	 */
	private String getKey(String moduleId, String copyId, String ownerId){
		return copyId+DELIMITER+moduleId+DELIMITER+ownerId;
	}
	
	/**
	 * Returns the cache key for given module. 
	 */
	private String getKey(Module module){
		return getKey(module.getId(), module.getCopyId(), module.getOwnerId());
	}

	@Override public void deleteModule(Module module) throws NoStorageForModuleException, StorageFailureException{
		deleteModule(module.getOwnerId(), module.getId(), module.getCopyId());
	}

	@Override public void deleteModule(String ownerId, String moduleId, String copyId)
		throws NoStorageForModuleException , StorageFailureException{
		removeFromCacheDirty(moduleId, ownerId, copyId);
		
	}

	@Override public void deleteModule(String ownerId, String moduleId)
		throws NoStorageForModuleException , StorageFailureException{
		deleteModule(ownerId, moduleId, DEFAULT_COPY_ID);
	}


	/**
	 * Removes changed module from cache and fires moduleLoaded event of registered listener.
	 * @param module changed module.
	 */
	@Override
	public void moduleLoaded(Module module){
		LOGGER.info("Persistence changed for " + module);
		removeFromCache(module.getId(),module.getOwnerId(),module.getCopyId());

		IModuleListener listener = moduleListeners.get(getKey(module.getId(), DEFAULT_COPY_ID, module.getOwnerId()));
		if (listener!=null)
			try{
				listener.moduleLoaded(module);
			}catch(Exception e){
				LOGGER.warn("Caught uncaught exception by the listener " + listener + ", contentChanged()", e);
			}
	}

	@Override
	public void addModuleListener(String moduleId, String ownerId, IModuleListener aModuleListeners){
		String key = getKey(moduleId, DEFAULT_COPY_ID, ownerId);

		moduleListeners.put(key, aModuleListeners);
	}

	/**
	 * Removes listener for module.
	 * @param moduleId id of module.
	 * @param ownerId  owner id for module.
	 */
	@Override
	public void removeModuleListener(String moduleId, String ownerId) {
		moduleListeners.remove(getKey(moduleId, DEFAULT_COPY_ID, ownerId));
	}

	@AfterConfiguration public void notifyConfigurationFinished() {
		LOGGER.info("Cleaning cache.");
		cache.clear();
	}

}
