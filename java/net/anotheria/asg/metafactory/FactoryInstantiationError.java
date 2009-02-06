package net.anotheria.asg.metafactory;

import net.anotheria.asg.service.ASGService;

public class FactoryInstantiationError extends MetaFactoryException{
	public FactoryInstantiationError(Class<? extends ServiceFactory<? extends ASGService>> clazz, String serviceName, String reason){
		super("Couldn't load factory of class "+clazz+" for service: "+serviceName+" because: "+reason);
	}
}
