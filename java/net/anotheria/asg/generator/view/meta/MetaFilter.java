package net.anotheria.asg.generator.view.meta;

public class MetaFilter implements Cloneable{
	
	private String name;
	private String className;
	private String fieldName;
	
	public MetaFilter(String aName, String aClassName){
		name = aName;
		className = aClassName;
	}
	
	
	public Object clone(){
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
	
	public String getClassNameOnly(){
		if (className.lastIndexOf('.')==-1)
			return className;
		return className.substring(className.lastIndexOf('.')+1);
	}

}
