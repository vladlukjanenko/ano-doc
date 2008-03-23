package net.anotheria.asg.util.rmi;

import java.rmi.registry.Registry;

import net.java.dev.moskito.core.configuration.ConfigurationServiceFactory;
import net.java.dev.moskito.core.configuration.IConfigurable;

public class RMIConfig implements IConfigurable{

	/**
	 * The name of the config key for registry host.
	 */
	private static final String KEY_REGISTRY_HOST = "registry.host";
	/**
	 * The name of the config key for registry port.
	 */
	private static final String KEY_REGISTRY_PORT = "registry.port";

	/**
	 * The host where the RMIRegistry is running.
	 */
	private String registryHost;
	/**
	 * The port where the RMIRegistry is running.
	 */
	private int    registryPort;
	
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
		ConfigurationServiceFactory.getConfigurationService().addConfigurable(this);
	}
		

	public String getConfigurationName() {
		return "rmi";
	}

	public void notifyConfigurationFinished() {
	}

	public void notifyConfigurationStarted() {
	}

	public void setProperty(String key, String value) {
		if (KEY_REGISTRY_PORT.equals(key))
			registryPort = Integer.parseInt(value);
		if (KEY_REGISTRY_HOST.equals(key))
			registryHost = value;
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
}
