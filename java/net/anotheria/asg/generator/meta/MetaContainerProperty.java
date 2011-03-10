package net.anotheria.asg.generator.meta;

/**
 * TODO please remined another to comment this class
 * @author another
 */
public class MetaContainerProperty extends MetaProperty{
	
	
	public MetaContainerProperty(String name){
		super(name, MetaProperty.Type.INT);
	}
	
	public MetaContainerProperty(String name, MetaProperty.Type type){
		super(name, type);
	}

	public String getContainerEntryName(){
		return getClass().getName().substring(getClass().getName().lastIndexOf('.')+1);	
	}
}
