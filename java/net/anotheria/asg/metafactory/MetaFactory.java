package net.anotheria.asg.metafactory;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import net.anotheria.asg.exception.ASGRuntimeException;
import net.anotheria.asg.service.ASGService;

public class MetaFactory {
	
	private static final Map<String, ASGService> instances = new HashMap<String, ASGService>();
	
	private static Map<String, String> aliases = new HashMap<String, String>();
	private static Map<String, Class<? extends ServiceFactory<? extends ASGService>>> factoryClasses = new HashMap<String, Class<? extends ServiceFactory<? extends ASGService>>>();
	
	
	
	
	public static <T extends ASGService> T create(Class<T> pattern, Extension extension){
		return null;
	}

	public static <T extends ASGService> T get(Class<T> pattern, Extension extension){
		
		if (extension==null)
			extension = Extension.NONE;
		String name = extension.toName(pattern);
		
		@SuppressWarnings("unchecked")
		T instance = (T) instances.get(name);
		
		if (instance!=null)
			return instance;
		
		return null;
	}
	
	public static final String resolveAlias(String name){
		String alias = aliases.get(name);
		return alias == null ? name : resolveAlias(alias);
	}
	
	public static final String resolveAlias(Class<? extends ASGService> clazz){
		return resolveAlias(clazz.getName());
	}

	public static final void addAlias(String name, String alias){
		aliases.put(alias, name);
	}
	
	public static <T extends ASGService> void addAlias(Class<T> pattern, Extension nameExtension){
		addAlias(pattern, nameExtension, null);
	}
	
	public static <T extends ASGService> void addAlias(Class<T> pattern, Extension nameExt, Extension aliasExtension){
		if (nameExt==null)
			nameExt = Extension.NONE;
		if (aliasExtension==null)
			aliasExtension = Extension.NONE;
		addAlias(nameExt.toName(pattern), aliasExtension.toName(pattern));
	}
	
	public static <T extends ASGService, F extends ServiceFactory<T>> void addFactoryClass(Class<T> service, Extension extension, Class<F> factoryClass){
		addFactoryClass(extension.toName(service), factoryClass);
	}
	
//	public static <T extends ASGService, F extends ServiceFactory<T>> void addFactoryClass(String serviceClassName, Extension extension, Class<F> factoryClass){
//		addFactoryClass(extension.toName(serviceClassName), factoryClass);
//	}

	public static <T extends ASGService, F extends ServiceFactory<T>>  void addFactoryClass(String name, Class<F> factoryClass){
		factoryClasses.put(name, factoryClass);
	}
	
	
	public static void debugDumpAliasMap(){
		Set<String> keys = aliases.keySet();
		for (String key : keys){
			System.out.println(key + " = "+aliases.get(key));
		}
	}
}
