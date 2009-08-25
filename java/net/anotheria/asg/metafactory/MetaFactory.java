package net.anotheria.asg.metafactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class MetaFactory {
	
	private static Map<String, Service> instances = new HashMap<String, Service>();
	
	private static Map<String, String> aliases = new HashMap<String, String>();
	
	private static Map<String, Class<? extends ServiceFactory<? extends Service>>> factoryClasses = new HashMap<String, Class<? extends ServiceFactory<? extends Service>>>();
	private static Map<String, ServiceFactory<? extends Service>> factories = new HashMap<String, ServiceFactory<? extends Service>>();
	
	
	private static final List<AliasResolver> resolverList;
	
	static{
		resolverList = new ArrayList<AliasResolver>();
		resolverList.add(new SystemPropertyResolver());
	}
	
	public static void reset(){
		factoryClasses = new HashMap<String, Class<? extends ServiceFactory<? extends Service>>>();
		factories = new HashMap<String, ServiceFactory<? extends Service>>();
		aliases = new HashMap<String, String>();
		instances = new HashMap<String, Service>();
	}
	
	
	
	public static <T extends Service> T create(Class<T> pattern, Extension extension)throws MetaFactoryException{
		return pattern.cast(create(extension.toName(pattern)));
	}

	public static <T extends Service> T create(Class<T> pattern)throws MetaFactoryException{
		return pattern.cast(create(pattern, Extension.NONE));
	}
///*
	@SuppressWarnings("unchecked")
	private static <T extends Service> T create(String name) throws MetaFactoryException{
		
		ServiceFactory<T> factory = (ServiceFactory<T>)factories.get(name);
		if (factory!=null)
			return factory.create();
		
		Class<? extends ServiceFactory<T>> clazz = (Class<? extends ServiceFactory<T>>)factoryClasses.get(name);
		if (clazz==null)
			throw new FactoryNotFoundException(name); 
		
		synchronized (factories) {
			factory = (ServiceFactory<T>) factories.get(name);
			if (factory==null){
				try{
					factory = clazz.newInstance();
					factories.put(name, factory);
				}catch(IllegalAccessException e){
					throw new FactoryInstantiationError(clazz, name, e.getMessage());
				}catch(InstantiationException e){
					e.printStackTrace();
					throw new FactoryInstantiationError(clazz, name, e.getMessage());
				}
			}
			
		}
		return factory.create();
	}
	//*/
	
	public static <T extends Service> T get(Class<T> pattern) throws MetaFactoryException{
		return get(pattern, Extension.NONE);
	}

	public static <T extends Service> T get(Class<T> pattern, Extension extension) throws MetaFactoryException{
		
		out("get called, pattern: "+pattern+", extension: "+extension);
		
		if (extension==null)
			extension = Extension.NONE;
		String name = extension.toName(pattern);
		out("name is "+name);
		
		name = resolveAlias(name);
		out("resolved alias to "+name);
		
		T instance = pattern.cast(instances.get(name));
		
		out("instance of "+name + " is: "+instance);
		
		if (instance!=null)
			return instance;
		
		synchronized (instances) {
			//double check
			//@SuppressWarnings("unchecked")
			instance = pattern.cast(instances.get(name));
			if (instance==null){
				out("creating new instance of "+name);
				instance = pattern.cast(create(name));
				out("created new instance of "+name+" ---> "+instance);
				instances.put(name, instance);
			}
		}
		
		return instance;
	}
	
	public static final String resolveAlias(String name){
		
		//first check resolvers
		synchronized(resolverList){
			for (AliasResolver resolver : resolverList){
				String resolved = resolver.resolveAlias(name);
				if (resolved!=null)
					return resolveAlias(resolved);
			}
		}
		
		String alias = aliases.get(name);
		return alias == null ? name : resolveAlias(alias);
	}
	
	public static final String resolveAlias(Class<? extends Service> clazz){
		return resolveAlias(clazz.getName());
	}

	public static final void addAlias(String name, String alias){
		aliases.put(alias, name);
	}
	
	public static <T extends Service> void addAlias(Class<T> pattern, Extension nameExtension){
		addAlias(pattern, nameExtension, null);
	}
	
	public static <T extends Service> void addAlias(Class<T> pattern, Extension nameExt, Extension aliasExtension){
		if (nameExt==null)
			nameExt = Extension.NONE;
		if (aliasExtension==null)
			aliasExtension = Extension.NONE;
		addAlias(nameExt.toName(pattern), aliasExtension.toName(pattern));
	}
	
	public static <T extends Service> void addFactoryClass(Class<T> service, Extension extension, Class<? extends ServiceFactory<T>> factoryClass){
		addFactoryClass(extension.toName(service), factoryClass);
	}
	
//	public static <T extends ASGService, F extends ServiceFactory<T>> void addFactoryClass(String serviceClassName, Extension extension, Class<F> factoryClass){
//		addFactoryClass(extension.toName(serviceClassName), factoryClass);
//	}

	public static <T extends Service>  void addFactoryClass(String name, Class<? extends ServiceFactory<T>> factoryClass){
		factoryClasses.put(name, factoryClass);
	}
	
	private static void out(Object o){
		
		//System.out.println("[MetaFactory] "+o);
	}
	
	public static void debugDumpAliasMap(){
		Set<String> keys = aliases.keySet();
		for (String key : keys){
			System.out.println(key + " = "+aliases.get(key));
		}
	}
	
	
	public static void addAliasResolver(AliasResolver resolver){
		synchronized(resolverList){
			for (int i=0; i<resolverList.size(); i++){
				AliasResolver someResolver = resolverList.get(i);
				if (resolver.getPriority()<someResolver.getPriority()){
					resolverList.add(i, resolver);
					return;
				}
			}
			resolverList.add(resolver);
		}
	}
	
	public static List<AliasResolver> getAliasResolverList(){
		synchronized(resolverList){
			ArrayList<AliasResolver> ret = new ArrayList<AliasResolver>();
			ret.addAll(resolverList);
			return ret;
		}
	}
	
}
