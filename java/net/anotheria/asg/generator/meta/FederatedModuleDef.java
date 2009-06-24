package net.anotheria.asg.generator.meta;
/**
 * Definition of a federated module. Each federated module is mapped to a key.
 * @author another
 *
 */
public class FederatedModuleDef{
	/**
	 * Name of the Module.
	 */
	private String name;
	/**
	 * Key of the module.
	 */
	private String key;

	FederatedModuleDef(String aKey, String aName){
		name = aName;
		key = aKey;
	}

	public String getKey() {
		return key;
	}

	public String getName() {
		return name;
	}
} 
