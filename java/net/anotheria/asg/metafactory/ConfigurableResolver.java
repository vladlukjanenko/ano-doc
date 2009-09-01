package net.anotheria.asg.metafactory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.configureme.ConfigurationManager;
import org.configureme.annotations.ConfigureMe;
import org.configureme.annotations.SetAll;

@ConfigureMe(name="factories",allfields=false,watch=true)
public class ConfigurableResolver implements AliasResolver{
	private Map<String,String> aliasMap;
	private int priority;

//	private static ConfigurableResolver instance;
//	
//	private static Object lock = new Object();
//    public static ConfigurableResolver getInstance(){
//		if(instance != null)
//			return instance;
//		synchronized(lock){
//			if(instance != null)
//				return instance;
//			instance = new ConfigurableResolver();
//			ConfigurationManager.INSTANCE.configure(instance);
//			return instance;
//		}
//	}
	
	public ConfigurableResolver(){
		priority = 50;
		aliasMap = new ConcurrentHashMap<String, String>();
		ConfigurationManager.INSTANCE.configure(this);
	}

	@SetAll
	public void addFactory(String name, String value) {
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
