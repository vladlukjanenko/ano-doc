package net.anotheria.asg.generator.view.meta;

/**
 * Definition of a decorator.
 * @author lrosenberg
 */
public class MetaDecorator implements Cloneable{
	/**
	 * The name of a decorator.
	 */
	private String name;
	/**
	 * The class name of the decorator.
	 */
	private String className;
	
	/**
	 * The rule for this decorator instance.
	 */
	private String rule;
	/**
	 * Creates a new metadecorator.
	 */
	public MetaDecorator(){
		
	}
	/**
	 * Creates a new meta decorator.
	 * @param aName
	 * @param aClassName
	 */
	public MetaDecorator(String aName, String aClassName){
		name = aName;
		className = aClassName;
	}
	
	@Override public Object clone(){
		try{
			return super.clone();
		}catch(Exception e){
			throw new AssertionError("Can't happen.");
		}
		
	}
	/**
	 * @return class name of the decorator
	 */
	public String getClassName() {
		return className;
	}

	/**
	 * @return name of a decorator
	 */
	public String getName() {
		return name;
	}

	/**
	 * @return rule for this decorator instance
	 */
	public String getRule() {
		return rule;
	}

	/**
	 * Sets class name for decorator.
	 * @param string class name
	 */
	public void setClassName(String string) {
		className = string;
	}

	/**
	 * Sets name for decorator.
	 * @param string name
	 */
	public void setName(String string) {
		name = string;
	}

	/**
	 * Sets rule for decorator instance.
	 * @param string rule
	 */
	public void setRule(String string) {
		rule = string;
	}
	
	@Override public String toString(){
		return name+" = "+className+" ("+rule+")";
	}
	
	@Override public boolean equals(Object o){
		return o instanceof MetaDecorator ?
			((MetaDecorator)o).getName().equals(name) : false; 
	}
	
	@Override public int hashCode(){
		return name == null ? 0 : name.hashCode();
	}
	
	/**
	 * @return the name of the class of the decorator without a package.
	 */
	public String getClassNameOnly(){
		if (className.lastIndexOf('.')==-1)
			return className;
		return className.substring(className.lastIndexOf('.')+1);
	}
}
