package net.anotheria.asg.metafactory;

public class FactoryNotFoundException extends MetaFactoryException{
	public FactoryNotFoundException(String name){
		super("No factory for service "+name+" found");
	}
}
