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
	 * Unique name of the module. Each module has a name which is used whenever someone refers to it.
	 */
	private String name;
	/**
	 * List of the documents in this module
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
	 * @return
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
	 * Returns contained documents.
	 * @return
	 */
	public List<MetaDocument> getDocuments() {
		return documents;
	}

	/**
	 * @return
	 */
	public String getName() {
		return name;
	}
	
	/**
	 * Returns the name for the module implementation class in the cms storage.
	 * @return
	 */
	public String getModuleClassName(){
		return "Module"+getName();
	}
	
	/**
	 * Returns the class name of the generated module factory.
	 * @return
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
	 * @return
	 */
	public String getId(){
		return getName().toLowerCase();
	}

	public MetaDocument getDocumentByName(String name){
	    for (int i=0; i<documents.size(); i++){
	        MetaDocument d = documents.get(i);
	        if (d.getName().equals(name))
	            return d;
	    }
	    throw new RuntimeException("No such document: "+name + " in module "+getName());
	}

	@Override public boolean equals(Object o){
		return o instanceof MetaModule ? 
			((MetaModule)o).name.equals(name) : false;
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

	public void setStorageType(StorageType storageType) {
		this.storageType = storageType;
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
	public void setStorageKey(String storageKey) {
		this.storageKey = storageKey;
	}
	
	public void addModuleParameter(ModuleParameter p){
		parameters.put(p.getName(), p);
	}
	
	public ModuleParameter getModuleParameter(String name){
		return parameters.get(name);
	}
	
	public boolean isParameterEqual(String name, String value){
		ModuleParameter p = getModuleParameter(name);
		return p == null ? false : p.getValue().equals(value);
	}

	public GenerationOptions getModuleOptions() {
		return moduleOptions;
	}

	public void setModuleOptions(GenerationOptions moduleOptions) {
		this.moduleOptions = moduleOptions;
	}
	
	
}
