package net.anotheria.asg.generator.meta;

public class FederatedModuleDef{
	private String name;
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
