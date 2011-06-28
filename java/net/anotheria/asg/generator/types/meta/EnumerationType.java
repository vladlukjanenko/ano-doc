package net.anotheria.asg.generator.types.meta;

import java.util.ArrayList;
import java.util.List;

/**
 * Internal representation of an enumeration type.
 * @author another
 */
public class EnumerationType extends DataType{
	/**
	 * List of possible values.
	 */
	private List<String> values;
	
	public EnumerationType(String name){
		super(name);
		values = new ArrayList<String>();
	}
	
	public void addValue(String aValue){
		values.add(aValue);
	}
	
	@Override public String toString(){
		return "Enumeration "+getName()+" :"+values;
	}
	/**
	 * @return possible values
	 */
	public List<String> getValues() {
		return values;
	}

	/**
	 * Sets possible values.
	 * @param list values
	 */
	public void setValues(List<String> list) {
		values = list;
	}

}
