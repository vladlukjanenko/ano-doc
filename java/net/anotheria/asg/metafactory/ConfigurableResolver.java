package net.anotheria.asg.metafactory;

import java.util.HashMap;
import java.util.Map;

import net.java.dev.moskito.core.configuration.ConfigurationServiceFactory;
import net.java.dev.moskito.core.configuration.IConfigurable;

public class ConfigurableResolver implements IConfigurable, AliasResolver{
	private String configurationName;
	private Map<String,String> aliasMap;
	private int priority;
	
	public ConfigurableResolver(String aConfigurationName, int aPriority){
		configurationName = aConfigurationName;
		priority = aPriority;
		ConfigurationServiceFactory.getConfigurationService().addConfigurable(this);
	}

	@Override
	public String getConfigurationName() {
		return configurationName;
	}

	@Override
	public void notifyConfigurationFinished() {
		
	}
	@Override
	public void notifyConfigurationStarted() {
		aliasMap = new HashMap<String, String>();
	}
	
	@Override
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
