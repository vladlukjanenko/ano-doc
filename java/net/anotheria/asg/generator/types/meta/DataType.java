package net.anotheria.asg.generator.types.meta;

import net.anotheria.asg.generator.IGenerateable;

/**
 * A custom data type.
 * @author lrosenberg
 */
public abstract class DataType implements IGenerateable{
	/**
	 * Name of the data type.
	 */
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
