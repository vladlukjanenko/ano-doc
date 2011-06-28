package net.anotheria.asg.generator.meta;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.anotheria.asg.generator.GenerationOptions;
import net.anotheria.asg.generator.GeneratorDataRegistry;
import net.anotheria.asg.generator.IGenerateable;

/**
 * Representation of a module definiton.
 * @author another
 */
public class MetaModule implements IGenerateable{

	/**
	 * This is a predefined module used to generate package and class names for shared stuff.
	 */
	public static final MetaModule SHARED = new MetaModule("Shared");
	
	/**
	 * This is a predefined module used to generate package and class names for user settings stuff.
	 */
	public static final MetaModule USER_SETTINGS = new MetaModule("UserSettings");
	

	/**
	 * Unique name of the module. Each module has a name which is used whenever someone refers to it.
	 */
	private String name;
	/**
	 * List of the documents in this module.
	 */
	private List<MetaDocument> documents;
	/**
	 * Module listeners which can be attached to the generated service.
	 */
	private List<String> listeners;
	/**
	 * Type of the storge for this module.
	 */
	private StorageType storageType;
	/**
	 * The sense of this parameter is lost in the depth of the code.
	 */
	private String storageKey;
	
	private Map<String, ModuleParameter> parameters;
	
	/**
	 * Generation options which can enable or disable generation of some artefacts.
	 */
	private GenerationOptions moduleOptions;
		 
	/**
	 * Creates a new empty module.
	 */
	public MetaModule(){
		this(null);
	}
	
	/**
	 * Creates a new module with the given name.
	 * @param name name of the module.
	 */
	public MetaModule(String name){
		this.name = name;
		documents = new ArrayList<MetaDocument>();
		listeners = new ArrayList<String>();
		storageType = StorageType.CMS;
		parameters = new HashMap<String, ModuleParameter>();
	}
	
	/**
	 * Adds a document definition to the module.
	 * @param aDocument
	 */
	public void addDocument(MetaDocument aDocument){
		documents.add(aDocument);
		aDocument.setParentModule(this);
	}
	
	/**
	 * Returns true if an option is enabled. For example 'rmi' is an option which can be enabled.
	 * @param key
	 * @return true if an option is enabled
	 */
	public boolean isEnabledByOptions(String key){
		if (moduleOptions!=null){
			if (moduleOptions.isEnabled(key))
				return true;
		}
		
		return GeneratorDataRegistry.getInstance().getOptions().isEnabled(key);
		
	}
	
	@Override public String toString(){
		return "module "+name+" storage: "+storageType+" documents: "+documents;
	}
	/**
	 * @return contained documents
	 */
	public List<MetaDocument> getDocuments() {
		return documents;
	}

	/**
	 * @return name of the module
	 */
	public String getName() {
		return name;
	}
	
	/**
	 * @return the name for the module implementation class in the cms storage
	 */
	public String getModuleClassName(){
		return "Module"+getName();
	}
	
	/**
	 * @return the class name of the generated module factory
	 */
	public String getFactoryClassName(){
		return getModuleClassName()+"Factory";
	}

	/**
	 * @param list
	 */
	public void setDocuments(List<MetaDocument> list) {
		documents = list;
	}

	/**
	 * @param string
	 */
	public void setName(String string) {
		name = string;
	}
	
	/**
	 * Returns the id of the module. Id is basically name.toLowerCase().
	 * @return the id of the module
	 */
	public String getId(){
		return getName().toLowerCase();
	}

	public MetaDocument getDocumentByName(String aName){
	    for (int i=0; i<documents.size(); i++){
	        MetaDocument d = documents.get(i);
	        if (d.getName().equals(aName))
	            return d;
	    }
	    throw new RuntimeException("No such document: "+aName + " in module "+getName());
	}

	@Override public boolean equals(Object o){
		return o instanceof MetaModule ? 
			((MetaModule)o).name.equals(name) : false;
	}
	
	@Override public int hashCode(){
		return name == null ? 42 : name.hashCode();
	}

	public List<String> getListeners() {
		return listeners;
	}

	public void setListeners(List<String> listeners) {
		this.listeners = listeners;
	}
	
	public void addListener(String listenerClass){
		listeners.add(listenerClass);
	}
	
	public void removeListener(String listenerClass){
		listeners.remove(listenerClass);
	}

	public StorageType getStorageType() {
		return storageType;
	}

	public void setStorageType(StorageType aStorageType) {
		storageType = aStorageType;
	}

	/**
	 * @deprecated Noone knows what storagekey does.
	 */
	public String getStorageKey() {
		return storageKey;
	}

	/**
	 * @deprecated Noone knows what storagekey does.
	 */
	public void setStorageKey(String aStorageKey) {
		storageKey = aStorageKey;
	}
	
	public void addModuleParameter(ModuleParameter p){
		parameters.put(p.getName(), p);
	}
	
	public ModuleParameter getModuleParameter(String aName){
		return parameters.get(aName);
	}
	
	public boolean isParameterEqual(String aName, String aValue){
		ModuleParameter p = getModuleParameter(aName);
		return p == null ? false : p.getValue().equals(aValue);
	}

	public GenerationOptions getModuleOptions() {
		return moduleOptions;
	}

	public void setModuleOptions(GenerationOptions someModuleOptions) {
		moduleOptions = someModuleOptions;
	}
	
	public boolean isContainsAnyMultilingualDocs(){
		for(MetaDocument doc:documents)
			if (GeneratorDataRegistry.hasLanguageCopyMethods(doc))
				return true;
		
		return false; 
	}
}
