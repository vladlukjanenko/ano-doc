package net.anotheria.asg.generator.view.meta;

/**
 * TODO please remined another to comment this class
 * @author another
 */
public class MetaDecorator implements Cloneable{
	private String name;
	private String className;
	
	private String rule;
	
	public MetaDecorator(){
		
	}
	
	public MetaDecorator(String aName, String aClassName){
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
	/**
	 * @return
	 */
	public String getClassName() {
		return className;
	}

	/**
	 * @return
	 */
	public String getName() {
		return name;
	}

	/**
	 * @return
	 */
	public String getRule() {
		return rule;
	}

	/**
	 * @param string
	 */
	public void setClassName(String string) {
		className = string;
	}

	/**
	 * @param string
	 */
	public void setName(String string) {
		name = string;
	}

	/**
	 * @param string
	 */
	public void setRule(String string) {
		rule = string;
	}
	
	public String toString(){
		return name+" = "+className+" ("+rule+")";
	}
	
	public boolean equals(Object o){
		return o instanceof MetaDecorator ?
			((MetaDecorator)o).getName().equals(name) : false; 
	}
	
	public String getClassNameOnly(){
		if (className.lastIndexOf('.')==-1)
			return className;
		return className.substring(className.lastIndexOf('.')+1);
	}
	

}
