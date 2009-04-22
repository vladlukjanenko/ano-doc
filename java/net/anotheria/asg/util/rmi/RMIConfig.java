package net.anotheria.asg.util.rmi;

import java.rmi.registry.Registry;

import org.configureme.annotations.ConfigureMe;

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
	
	public String toString(){
		return "RMIConfig "+getRegistryHost()+":"+getRegistryPort();
	}

	public void setRegistryHost(String registryHost) {
		this.registryHost = registryHost;
	}

	public void setRegistryPort(int registryPort) {
		this.registryPort = registryPort;
	}
}
