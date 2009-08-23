package net.anotheria.asg.metafactory;


public class FactoryInstantiationError extends MetaFactoryException{
	public FactoryInstantiationError(Class<? extends ServiceFactory<? extends Service>> clazz, String serviceName, String reason){
		super("Couldn't load factory of class "+clazz+" for service: "+serviceName+" because: "+reason);
	}
}
