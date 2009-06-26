package net.anotheria.asg.generator;

/**
 * Represents a free parameter attachable to the context.
 * @author another
 *
 */
public class ContextParameter {
	/**
	 * Name of the parameter.
	 */
	private String name;
	/**
	 * Value of the parameter.
	 */
	private String value;
	
	/**
	 * Constant for cms versioning parameter.
	 */
	public static final String CTX_PARAM_CMS_VERSIONING = "cmsversioning";
	
	public ContextParameter(){
		
	}
	
	public ContextParameter(String aName, String aValue){
		name = aName;
		value = aValue;
	}
	
	@Override public String toString(){
		return "Parameter "+name+" = "+value;
	}

	public String getName() {
		return name;
	}

	public void setName(String aName) {
		name = aName;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String aValue) {
		value = aValue;
	}
}
