package net.anotheria.asg.generator;

import java.util.HashMap;
import java.util.Map;

public class GenerationOptions {
	
	public static final String RMI = "rmi";
	public static final String INMEMORY = "inmemory";
	
	private Map<String, GenerationOption> options;
	
	public GenerationOptions(){
		options = new HashMap<String, GenerationOption>();
	}
	
	public GenerationOption get(String key){
		return options.get(key);
	}
	
	public boolean isEnabled(String key){
		GenerationOption v = options.get(key);
		return v!=null && v.getValue().equalsIgnoreCase("true");
	}
	
	public void set(String key, String value){
		options.put(key, new GenerationOption(key,value));
	}
	
	public void set(GenerationOption option){
		options.put(option.getName(), option);
	}
}
