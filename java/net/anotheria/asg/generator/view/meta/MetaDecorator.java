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
	 * Returns the name of the class of the decorator without a package.
	 * @return
	 */
	public String getClassNameOnly(){
		if (className.lastIndexOf('.')==-1)
			return className;
		return className.substring(className.lastIndexOf('.')+1);
	}
}
