package net.anotheria.asg.generator;

public class GenerationOption {
	private String name;
	private String value;
	
	public GenerationOption(String aName, String aValue){
		name = aName;
		value = aValue;
	}
	
	public GenerationOption(){
		this("","");
	}
	
	public String toString(){
		return name+" = "+value;
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
	
	public boolean isTrue(){
		return value!=null && value.equalsIgnoreCase("true");
	}

	public boolean isFalse(){
		return value!=null && !(value.equalsIgnoreCase("true"));
	}
	
	public boolean isSet(){
		return value!=null;
	}

}
