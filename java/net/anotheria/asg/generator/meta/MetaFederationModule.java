package net.anotheria.asg.generator.meta;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class MetaFederationModule extends MetaModule {

	private List<FederatedModuleDef> federatedModules;
	private Map<String, List<FederatedDocumentMapping>> mappings;
	
	public MetaFederationModule(){
		this(null);
	}
	
	public MetaFederationModule(String name){
		super(name);
		setStorageType( StorageType.FEDERATION);
		federatedModules = new ArrayList<FederatedModuleDef>();
		mappings = new HashMap<String, List<FederatedDocumentMapping>>();
	}
	
	public void addFederatedModule(String aKey, String aName){
		federatedModules.add(new FederatedModuleDef(aKey, aName));
	}
	
	
	public List<FederatedModuleDef> getFederatedModules() {
		return federatedModules;
	}

	public void setFederatedModules(List<FederatedModuleDef> federatedModules) {
		this.federatedModules = federatedModules;
	}
	
	public void addMapping(FederatedDocumentMapping mapping){
		List<FederatedDocumentMapping> mappingsForDocument = mappings.get(mapping.getSourceDocument());
		if (mappingsForDocument==null){
			mappingsForDocument = new ArrayList<FederatedDocumentMapping>();
			mappings.put(mapping.getSourceDocument(), mappingsForDocument);
		}
		mappingsForDocument.add(mapping);
	}
	
	public List<FederatedDocumentMapping> getMappingsForDocument(String documentName){
		List<FederatedDocumentMapping> ret = mappings.get(documentName);
		return ret == null ? new ArrayList<FederatedDocumentMapping>() : ret;
	}

}

