package net.anotheria.asg.util.rmi;

import java.rmi.registry.Registry;

import org.configureme.annotations.ConfigureMe;

/**
 * Configuration for rmi services in ano-doc. This config file is configured by the configureme framework.
 * @author lrosenberg
 *
 */
@ConfigureMe (allfields=true)
public class RMIConfig {


	/**
	 * The host where the RMIRegistry is running.
	 */
	private String registryHost;

	/**
	 * The port where the RMIRegistry is running.
	 */
	private int  registryPort;
	
	/**
	 * Default registry host value if nothing is explicitely configured.
	 */
	public static final String DEF_REGISTRY_HOST = "localhost";
	/**
	 * Default registry port value if nothing is explicitely configured.
	 */
	public static final int DEF_REGISTRY_PORT = Registry.REGISTRY_PORT;
	
	/**
	 * Creates a new config.
	 */
	RMIConfig(){
		registryHost = DEF_REGISTRY_HOST;
		registryPort = DEF_REGISTRY_PORT;
	}
		
	public String getRegistryHost(){
		return registryHost;
	}
	
	public int getRegistryPort(){
		return registryPort;
	}
	
	@Override public String toString(){
		return "RMIConfig "+getRegistryHost()+":"+getRegistryPort();
	}

	public void setRegistryHost(final String aRegistryHost) {
		registryHost = aRegistryHost;
	}

	public void setRegistryPort(final int aRegistryPort) {
		registryPort = aRegistryPort;
	}
}
