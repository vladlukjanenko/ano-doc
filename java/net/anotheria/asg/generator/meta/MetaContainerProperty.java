package net.anotheria.asg.generator.meta;

/**
 * TODO please remined another to comment this class
 * @author another
 */
public class MetaContainerProperty extends MetaProperty{
	public MetaContainerProperty(String name){
		super(name, "int");
	}
	
	public String getContainerEntryName(){
		return getClass().getName().substring(getClass().getName().lastIndexOf('.')+1);	
	}
}
