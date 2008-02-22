package net.anotheria.anodoc.query2;

public class QueryLikeProperty extends QueryProperty{
	public QueryLikeProperty(String aName, Object aValue){
		super(aName, aValue);
	}
	
	public String getComparator(){
		return " like ";
	}

	@Override
	public Object getValue() {
		return "%"+super.getValue()+"%";
	}

	public boolean doesMatch(Object o){
		return o== null ?getOriginalValue() == null :
			o.toString().indexOf(getOriginalValue().toString()) != -1;
	}

}
