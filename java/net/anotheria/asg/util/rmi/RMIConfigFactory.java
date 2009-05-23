package net.anotheria.asg.util.rmi;

import org.configureme.ConfigurationManager;

/**
 * The factory for the RMIConfig.
 */
public class RMIConfigFactory {
	
	/**
	 * The singleton instance of the rmiconfig.
	 */
	private static final RMIConfig instance;
	
	static{
		instance = new RMIConfig();
		ConfigurationManager.INSTANCE.configure(instance);
	}
	
	/**
	 * Returns the RMIConfig.
	 * @return
	 */
	public static final RMIConfig getRMIConfig(){
		return instance;
	}
}
