package net.anotheria.asg.generator.types.meta;

import net.anotheria.asg.generator.IGenerateable;

/**
 * TODO please remined another to comment this class
 * @author another
 */
public abstract class DataType implements IGenerateable{
	private String name;
	
	protected DataType(String aName){
		name = aName;
	}
	
	/**
	 * @return
	 */
	public String getName() {
		return name;
	}

	/**
	 * @param string
	 */
	public void setName(String string) {
		name = string;
	}
	
	

}
