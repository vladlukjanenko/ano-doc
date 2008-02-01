package net.anotheria.anodoc.query2;

public class QueryStartsWithProperty extends QueryProperty{
	public QueryStartsWithProperty(String aName, Object aValue){
		super(aName, aValue);
	}
	
	public String getComparator(){
		return " like ";
	}

	@Override
	public Object getValue() {
		return super.getValue()+"%";
	}
	
}
