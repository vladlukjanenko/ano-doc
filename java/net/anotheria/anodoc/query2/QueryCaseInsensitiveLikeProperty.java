package net.anotheria.anodoc.query2;

public class QueryCaseInsensitiveLikeProperty extends QueryLikeProperty{
	public QueryCaseInsensitiveLikeProperty(String aName, Object aValue){
		super(aName, aValue);
	}
	
	public String getComparator(){
		return " ilike ";
	}

}
