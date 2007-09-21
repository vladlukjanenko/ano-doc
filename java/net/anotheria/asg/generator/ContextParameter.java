package net.anotheria.asg.generator;

public class ContextParameter {
	private String name;
	private String value;
	
	public static final String CTX_PARAM_CMS_VERSIONING = "cmsversioning";
	
	public ContextParameter(){
		
	}
	
	public ContextParameter(String aName, String aValue){
		name = aName;
		value = aValue;
	}
	
	public String toString(){
		return "Parameter "+name+" = "+value;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}
}
