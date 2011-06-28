package net.anotheria.asg.generator.view.meta;
/**
 * Definition of a filter.
 * @author another
 *
 */
public class MetaFilter implements Cloneable{
	/**
	 * Human readable name of the filter.
	 */
	private String name;
	/**
	 * The name of the filter realization class.
	 */
	private String className;
	/**
	 * Name of the field the filter applies to.
	 */
	private String fieldName;
	
	public MetaFilter(String aName, String aClassName){
		name = aName;
		className = aClassName;
	}
	
	@Override public Object clone(){
		try{
			return super.clone();
		}catch(Exception e){
			return null;
		}
	}

	public String getFieldName() {
		return fieldName;
	}

	public void setFieldName(String fieldName) {
		this.fieldName = fieldName;
	}
	
	public String getName(){
		return name;
	}
	
	public void setName(String aName){
		name = aName;
	}
	
	public String getClassName(){
		return className;
	}

	/**
	 * Extracts the classname from the fully qualified class name.
	 * @return class name
	 */
	public String getClassNameOnly(){
		if (className.lastIndexOf('.')==-1)
			return className;
		return className.substring(className.lastIndexOf('.')+1);
	}

}
