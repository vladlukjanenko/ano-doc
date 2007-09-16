package net.anotheria.asg.generator.meta;

import java.util.ArrayList;
import java.util.List;

import net.anotheria.asg.generator.IGenerateable;

/**
 * TODO please remined another to comment this class
 * @author another
 */
public class MetaModule implements IGenerateable{
	private String name;
	private List<MetaDocument> documents;
	//for now listeners are simply class name implementing IServiceListener, extend it to various listener in the future if needed.
	private List<String> listeners;
	
	private StorageType storageType;
	private String storageKey;
	 
	public MetaModule(){
		this(null);
	}
	
	public MetaModule(String name){
		this.name = name;
		documents = new ArrayList<MetaDocument>();
		listeners = new ArrayList<String>();
		storageType = StorageType.CMS;
	}
	
	public void addDocument(MetaDocument aDocument){
		documents.add(aDocument);
		aDocument.setParentModule(this);
	}
	
	public String toString(){
		return "module "+name+" storage: "+storageType+" documents: "+documents;
	}
	/**
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
	
	public String getModuleClassName(){
		return "Module"+getName();
	}
	
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
	
	public String getId(){
		return getName().toLowerCase();
	}

	public MetaDocument getDocumentByName(String name){
	    for (int i=0; i<documents.size(); i++){
	        MetaDocument d = documents.get(i);
	        if (d.getName().equals(name))
	            return d;
	    }
	    throw new RuntimeException("No such document: "+name);
	}

	public boolean equals(Object o){
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

	public String getStorageKey() {
		return storageKey;
	}

	public void setStorageKey(String storageKey) {
		this.storageKey = storageKey;
	}
	
	public static final MetaModule SHARED = new MetaModule("Shared");
	
}
