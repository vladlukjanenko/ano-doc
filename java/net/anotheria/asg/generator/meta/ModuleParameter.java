package net.anotheria.asg.generator.meta;

public class ModuleParameter {
	private String name;
	private String value;
	
	public static final String MODULE_DB_CONTEXT_SENSITIVE = "db_context_sensitive";
	
	public ModuleParameter(){
		
	}
	
	public ModuleParameter(String aName, String aValue){
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
