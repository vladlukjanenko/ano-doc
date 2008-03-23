package net.anotheria.asg.util.rmi;

public class RMIConfigFactory {
	
	private static final RMIConfig instance = new RMIConfig();
	
	public static final RMIConfig getRMIConfig(){
		return instance;
	}
}
