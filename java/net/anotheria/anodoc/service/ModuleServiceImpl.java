package net.anotheria.anodoc.service;

import java.util.Hashtable;
import java.util.List;

import org.apache.log4j.Logger;

import net.anotheria.anodoc.data.Module;
import net.anotheria.anodoc.query.Predicate;
import net.anotheria.anodoc.stats.IStatisticsConstants;
import net.anotheria.anodoc.stats.ModuleStatistics;
import net.anotheria.anodoc.stats.StatisticsFactory;
import net.anotheria.anodoc.util.CommonHashtableModuleStorage;
import net.java.dev.moskito.core.configuration.ConfigurationServiceFactory;
import net.java.dev.moskito.core.configuration.IConfigurable;


/**
 * An implementation of IModuleService for local usage, 
 * which supports local cache and synchronization over network.
 */
public class ModuleServiceImpl implements IModuleService,IConfigurable/*, EventPushConsumerIFC*/{
	
	/**
	 * The name of the channel used for internal communication.
	 */
	//private static final String MY_CHANNEL_NAME = "anodoc.IModuleServiceChannel";
	
	private static final String DELIMITER = "#";
	
	public static final String DEFAULT_COPY_ID = "singlecopy";

//	private EventChannel receiveChannel, sendChannel;

	private static Logger log;
	static {
		log = Logger.getLogger(ModuleServiceImpl.class);
	}	 
	 
	/** 
	 * The factories.
	 */
	private Hashtable<String, IModuleFactory> factories;

	/**
	 * The module storages.
	 */
	private Hashtable<String, IModuleStorage> storages;

	/**
	 * The local cache - loaded modules.
	 */
	private Hashtable<String,Module> cache;

	/**
	 * The current instance. ModuleServiceImpl is a Singleton.
	 */
	private static ModuleServiceImpl instance;
	
	/**
	 * Returns the current (singleton) instance of this implementation. 
	 */
	protected static ModuleServiceImpl getInstance(){
		if (instance==null){
			instance = new ModuleServiceImpl();
		}
		return instance;
	}

	/**
	 * Creates a new ModuleServiceImpl.
	 */
	private ModuleServiceImpl(){
		//initialize local data.
		factories = new Hashtable<String, IModuleFactory>();
		storages  = new Hashtable<String, IModuleStorage>();
		cache = new Hashtable<String, Module>();		
		if (log.isDebugEnabled()) {
			log.debug("Created new ModuleServiceImplementation");
		}

		//attach statistic module
		attachStatistics();

		//initialize event channels
//		receiveChannel = LocalEventService.getInstance().obtainEventChannel(MY_CHANNEL_NAME,ESConstants.MODE_PUSH_CONSUMER);
//		receiveChannel.addConsumer(this);
//		sendChannel = LocalEventService.getInstance().obtainEventChannel(MY_CHANNEL_NAME,ESConstants.MODE_PUSH_SUPPLIER);
//		log.info("receiveChannelClass: "+receiveChannel.getClass());
//		log.info("sendChannelClass: "+sendChannel.getClass());
		
		ConfigurationServiceFactory.getConfigurationService().addConfigurable(this);
	}
	
	
	

	/**
	 * Attaches a factory for given module id.
	 * @see biz.beaglesoft.bgldoc.service.IModuleService#attachModuleFactory(String, IModuleFactory)
	 */
	public void attachModuleFactory(String moduleId, IModuleFactory factory) {
		factories.put(moduleId, factory);
		if (log.isDebugEnabled()) {
			log.debug("Attached module factory "+factory+" for moduleId:"+moduleId);
		}
	}
	
	/**
	 * Attaches a storage for given module id.
	 * @see biz.beaglesoft.bgldoc.service.IModuleService#attachModuleStorage(String, IModuleStorage)
	 */
	public void attachModuleStorage(String moduleId, IModuleStorage storage){
		storages.put(moduleId, storage);
		if (log.isDebugEnabled()) {
			log.debug("Attached module storage "+storage+" for moduleId:"+moduleId);
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
	 * Put in cache and store.
	 */
	private void putInCacheDirty(Module module) throws NoStorageForModuleException, StorageFailureException{
		putInCache(module);
		IModuleStorage storage = storages.get(module.getId());
		if (storage==null){
			log.warn("No storage for "+module.getId()+", "+module+" is not persistent!");
			throw new NoStorageForModuleException(module.getId());
		}
		storage.saveModule(module);
		//sendEvent(module);
	}
	
	/**
     * Send an event concerning module change.
	 */
/*	private void sendEvent(BGLModule module) {
		BGLDocStoreEvent e = new BGLDocStoreEvent();
		//!!! TODO remove cip dependency
		e.setInstanceId(CIPEngine.getInstance().getCIPId());
		e.setCopyId(module.getCopyId());
		e.setModuleId(module.getId());
		e.setOwnerId(module.getOwnerId());
		Event event = new Event(e);
		sendChannel.push(event);
	}
*/
	/**
	 * Called by event service.
	 */
/*	public void push(Event event) {
		log.info("received event");
		BGLDocStoreEvent e = (BGLDocStoreEvent)event.data;
		//!!! TODO remove cip dependency
		if(e.getInstanceId() == CIPEngine.getInstance().getCIPId()){
			//ignore: it from my self
			return;
		}
		removeFromCache(e.getModuleId(),e.getOwnerId(),e.getCopyId());
	}
*/
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
	public Module getModule(
		String ownerId,
		String moduleId,
		String copyId) 
			throws NoStorageForModuleException, NoFactoryForModuleException, NoStoredModuleEntityException, StorageFailureException{
		return getModule(ownerId, moduleId, copyId, false);
	}

	/**
	 * Same as getModule(ownerId, moduleId, default_copy_id, false)
	 */
	public Module getModule(
		String ownerId,
		String moduleId) 
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
	

	/**
	 * @see biz.beaglesoft.bgldoc.service.IModuleService#getModule(String, String, String, boolean)
	 */
	public Module getModule(
		String ownerId,
		String moduleId,
		String copyId,
		boolean create) 
			throws NoStorageForModuleException, NoFactoryForModuleException, NoStoredModuleEntityException, StorageFailureException{
		
		
		String key = getKey(moduleId, copyId, ownerId);
		//System.out.println("getModule() "+key+" called");

//		if (log.isDebugEnabled()) {
//			log.debug("Requested module:"+moduleId+", user:"+ownerId+", copy:"+copyId);
//			log.debug("Key: "+key);
//		}
		
		//first we check if we have this module in cache.
		Module module = (Module)cache.get(key);
		
		//TODO n�chste zeile reinkommentieren um die bremse wieder einzubauen.
		//log.debug("cache: " + cache);
		//System.out.println("getModule() in cache: "+module);
		if (module!=null){
			log.debug("Module "+key+" was in cache");
			return module;
		}
		
		
		try{
			log.debug("Trying to load module from storage:"+key);
			module = loadModule(moduleId, ownerId, copyId);
			//System.out.println("Loading from disk.");
			log.debug("Loaded module from storage.");
			putInCache(module);
			//long en = System.currentTimeMillis();
			return module;
		}catch(NoStoredModuleEntityException e){
			//log.debug("Loading failed:",e);
			if (create){
				log.debug("Creating new instance of "+moduleId+", "+ownerId+", "+copyId);
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
			
			log.debug("Loading failed:",e);
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

	/**
	 * @see biz.beaglesoft.bgldoc.service.IModuleService#storeModule(BGLModule, String)
	 */
	public void storeModule(Module module) throws NoStorageForModuleException, StorageFailureException{
		putInCacheDirty(module);
		if (!module.getId().equals(IStatisticsConstants.MODULE_STATISTICS)){
			updateStatistics(module);
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

	/**
	 * @see biz.beaglesoft.bgldoc.service.IModuleService#deleteModule(BGLModule)
	 */
	public void deleteModule(Module module) throws NoStorageForModuleException, StorageFailureException{
		deleteModule(module.getOwnerId(), module.getId(), module.getCopyId());
	}

	/**
	 * @see biz.beaglesoft.bgldoc.service.IModuleService#deleteModule(String, String, String)
	 */
	public void deleteModule(String ownerId, String moduleId, String copyId)
		throws NoStorageForModuleException , StorageFailureException{
		removeFromCacheDirty(moduleId, ownerId, copyId);
		
	}

	public void deleteModule(String ownerId, String moduleId)
		throws NoStorageForModuleException , StorageFailureException{
		deleteModule(ownerId, moduleId, DEFAULT_COPY_ID);
	}

	/**
	 * @see biz.beaglesoft.bgldoc.service.IModuleService#executeQueryOnDocuments(BasicPredicate)
	 */
	public List executeQueryOnDocuments(String moduleId, Predicate p) throws NoStorageForModuleException, StorageFailureException{
		IModuleStorage storage = (IModuleStorage)storages.get(moduleId);
		if (storage==null){
			log.warn("No storage for "+moduleId);
			throw new NoStorageForModuleException(moduleId);
		}
		return storage.executeQueryOnDocuments(p);

	}

	/**
	 * Configures and attaches the statistics module
	 *
	 */
	private void attachStatistics(){
		//now configure myself for statistics.
		AbstractModuleFactory factory = new StatisticsFactory();
		String moduleId = IStatisticsConstants.MODULE_STATISTICS;
		attachModuleFactory(moduleId, factory);
		attachModuleStorage(moduleId, new CommonHashtableModuleStorage(moduleId+".dat", factory));
	}
	
	/**
	 * Updates statistics with the statistical information from the given module. Also updates the size of the module 
	 * in the statistic module for quota calculation. 
	 */
	private void updateStatistics(Module module){
		String ownerId = module.getOwnerId();
		String copyId  = module.getCopyId();

		try{		
			ModuleStatistics stats = (ModuleStatistics)getModule(ownerId, IStatisticsConstants.MODULE_STATISTICS, copyId, true);
			if (module.getStatisticalInformation()!=IStatisticsConstants.NO_STATS)
				stats.updateStatisticValue(module.getId(), module.getStatisticalInformation());
			stats.updateSizeValue(module.getId(), module.getSizeInBytes());
			storeModule(stats);
		}catch(Exception e){
			log.error("updateStatistics", e);
		}
	}

	public String getConfigurationName() {
		return "anodoc.storage";
	}

	public void notifyConfigurationFinished() {
		log.info("Cleaning cache.");
		cache.clear();
		//System.out.println("CONFIG FINISHED!");
	}

	public void notifyConfigurationStarted() {
	}

	public void setProperty(String arg0, String arg1) {
	}

}