package net.anotheria.asg.metafactory;

import java.util.HashMap;
import java.util.Map;

public class ConfigurableResolver implements AliasResolver{
	private String configurationName;
	private Map<String,String> aliasMap;
	private int priority;
	
	public ConfigurableResolver(){
		this("factories", 50);
	}
	
	public ConfigurableResolver(String aConfigurationName, int aPriority){
		configurationName = aConfigurationName;
		priority = aPriority;
		throw new AssertionError("Not implemented, must be migrated to ConfigureMe");
		//ConfigurationServiceFactory.getConfigurationService().addConfigurable(this);
	}

	public String getConfigurationName() {
		return configurationName;
	}
	public void notifyConfigurationFinished() {
		
	}
	public void notifyConfigurationStarted() {
		aliasMap = new HashMap<String, String>();
	}
	
	public void setProperty(String name, String value) {
		aliasMap.put(name,value);
	}

	@Override
	public int getPriority() {
		return priority;
	}

	@Override
	public String resolveAlias(String alias) {
		return aliasMap.get(alias);
	}
}
