package net.anotheria.asg.util.rmi;

import org.configureme.ConfigurationManager;

public class RMIConfigFactory {
	
	private static final RMIConfig instance;
	
	static{
		instance = new RMIConfig();
		ConfigurationManager.INSTANCE.configure(instance);
	}
	
	public static final RMIConfig getRMIConfig(){
		return instance;
	}
}
