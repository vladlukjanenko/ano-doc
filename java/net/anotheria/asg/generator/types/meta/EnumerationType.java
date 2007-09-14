package net.anotheria.asg.generator.types.meta;

import java.util.ArrayList;
import java.util.List;

/**
 * TODO please remined another to comment this class
 * @author another
 */
public class EnumerationType extends DataType{
	private List<String> values;
	
	public EnumerationType(String name){
		super(name);
		values = new ArrayList<String>();
	}
	
	public void addValue(String aValue){
		values.add(aValue);
	}
	
	public String toString(){
		return "Enumeration "+getName()+" :"+values;
	}
	/**
	 * @return
	 */
	public List<String> getValues() {
		return values;
	}

	/**
	 * @param list
	 */
	public void setValues(List<String> list) {
		values = list;
	}

}
