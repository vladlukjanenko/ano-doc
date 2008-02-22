package net.anotheria.anodoc.query2;

public class QueryProperty {
	private String name;
	private Object value;
	
	public QueryProperty(String aName, Object aValue){
		name = aName;
		value = aValue;
	}
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public Object getValue() {
		return value;
	}
	public void setValue(Object value) {
		this.value = value;
	}
	
	public String toString(){
		return getName() + "=" +getValue();
	}
	
	public String getComparator(){
		return " = ";
	}
	
	public boolean doesMatch(Object o){
		return o== null ? value == null :
			o.equals(value);
	}
	
	protected Object getOriginalValue(){
		return value;
	}
	
}
